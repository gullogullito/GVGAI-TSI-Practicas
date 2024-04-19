package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class AgenteAStar extends AbstractPlayer {

	Vector2d fescala;
	Vector2d portal;

	PriorityQueue<NodoA> abiertos; // Nodos abiertos
	HashMap<CoordenadasLLave, Integer> cerrados; // Nodos ya visitados
	Stack<Types.ACTIONS> ruta; // Pila con la ruta, siempre sacamos la accion mas longeva
	boolean tengoRuta; // 0 si no hay ruta planeada, 1 caso contrario
	NodoA meta;
	HashSet<Coordenadas> obstaculos;
	Integer nNodos;
	ArrayList<Observation>[][] mygrid;
	ArrayList<Observation> muros, trampas;
	

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// Calculamos el factor de escala entre mundos (pixeles -> grid)
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		// Se crea una lista de observaciones de portales, ordenada por cercania al
		// avatar
		ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
		// Seleccionamos el portal mas proximo
		portal = posiciones[0].get(0).position;
		portal.x = Math.floor(portal.x / fescala.x);
		portal.y = Math.floor(portal.y / fescala.y);

		ruta = new Stack<>();
		abiertos = new PriorityQueue<NodoA>();
		cerrados = new HashMap<CoordenadasLLave, Integer>();
		tengoRuta = false;
		
		mygrid = stateObs.getObservationGrid();
		muros = new ArrayList<Observation>();
		trampas = new ArrayList<Observation>();
		if(stateObs.getImmovablePositions() != null) {
			if( stateObs.getImmovablePositions().length > 0)
				muros = stateObs.getImmovablePositions()[0];
			if( stateObs.getImmovablePositions().length > 1)
				trampas = stateObs.getImmovablePositions()[1];
		}
		obstaculos = new HashSet<Coordenadas>();
		
		//Guardamos todos los obstáculos, tanto muros como trampas en un HashSet, para hacer eficiente su búsqueda
		if(muros.size() > 0) {
			for ( int i = 0; i < muros.size() ; i++) {
				Vector2d posicion = muros.get(i).position;
				Coordenadas pos_aux = new Coordenadas(Math.floor(posicion.x/fescala.x), Math.floor(posicion.y/fescala.y));
				obstaculos.add(pos_aux);
			}
		}
		if(trampas.size() > 0) {
			for ( int i = 0; i < trampas.size() ; i++) {
				Vector2d posicion = trampas.get(i).position;
				Coordenadas pos_aux = new Coordenadas(Math.floor(posicion.x/fescala.x), Math.floor(posicion.y/fescala.y));
				obstaculos.add(pos_aux);
			}
		}
		nNodos = 0;

	}

	/**
	 * return the best action to arrive faster to the closest portal
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return best ACTION to arrive faster to the closest portal
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		if (!tengoRuta) {

			long tInicio = System.nanoTime();

			AStar(stateObs);

			long tFin = System.nanoTime();

			long tiempoTotalms = (tFin - tInicio) / 1000000;

			System.out.print("\nRuntime acumulado :  ");System.out.print(tiempoTotalms);
			
			if (ruta.isEmpty()) {
				//He encontrado la meta, guardando sus mejores padres
				NodoA aux = meta;
				ACTIONS anterior = ACTIONS.ACTION_NIL; // Ultima accion la inicializo a NIL
				
				ruta.push(meta.getAccion()); // Hago push de la ultima accion, justo la que me lleva a la meta, pues en
				// Dijkstra no la meto

				//Voy recorriendo de última a primera las acciones de los nodos, hasta dar con el padre null (nodo anterior al inicial)
				while (aux != null) {

					if (aux.getPadre() != null) {
						// Si son acciones distintas, tengo en cuenta el giro y añado dos veces la
						// accion
						if (anterior != aux.getAccion() && anterior != ACTIONS.ACTION_NIL) {
							ruta.push(aux.getAccion());
							ruta.push(aux.getAccion());
							anterior = aux.getAccion();
						} else {
							ruta.push(aux.getAccion());
							anterior = aux.getAccion();
						}

					}
					// Voy un nodo hacia atrás
					aux = aux.getPadre();
				}
				
				if (ruta.peek() == ACTIONS.ACTION_RIGHT) { // Si la primera accion coincide con la orientación inicial
					ruta.pop(); // hacemos pop(), porque si no va a añadir una de más
				}
				
				System.out.print("\nTamaño de la ruta :  ");System.out.print(ruta.size());
				System.out.print("\nNodos expandidos :  ");System.out.print(nNodos);
				System.out.print("\n\n");
			}

			return ruta.pop();
		} else {
			return ruta.pop();
		}

	}

	// Método que implementa A*
	private void AStar(StateObservation stateObs) {

		// Inicialmente calculamos la posicion del avatar, creamos el nodo padre, que es
		// la casilla y posicion de salida (con
		// coste 0) y lo añadimos a los nodos abiertos
		CoordenadasLLave pos = new CoordenadasLLave	(stateObs.getAvatarPosition().x / fescala.x,
				stateObs.getAvatarPosition().y / fescala.y, orientationToAction(stateObs.getAvatarOrientation()));
		NodoA inicial = new NodoA(null, orientationToAction(stateObs.getAvatarOrientation()), pos, 0,
				dMan(pos, portal));
		NodoA expandidos[];
		abiertos.add(inicial);
		NodoA sucesor;

		while (!tengoRuta && !abiertos.isEmpty()) {

			NodoA actual = abiertos.poll();

			if (actual.pos.x == portal.x && actual.pos.y == portal.y) {
				meta = actual;
				tengoRuta = true;
			} else {

				nNodos++;
				cerrados.put(actual.getPos(), actual.getG());

				expandidos = actual.expandir(portal);

				for (int i = 0; i < expandidos.length; i++) {
					sucesor = expandidos[i];

					if (puedoMover(sucesor.getPos().getCoordenadas())) {
						if (cerrados.containsKey(sucesor.getPos()) && mejorCaminoA(sucesor, 0)) { // Aqui nunca va a
																								// entrar porque nuestra
																								// heurística es
							actualizarCerrados(sucesor);										// monótona
							
						}
						else if (!cerrados.containsKey(sucesor.getPos()) && !abiertos.contains(sucesor))
							abiertos.add(sucesor);
						else if (abiertos.contains(sucesor) && mejorCaminoA(sucesor, 1))
							actualizarAbiertos(sucesor);
					}
				}
			}
		}
	}

	// Funcion que mira en cerrados o en abiertos (0 o 1, resp) si el sucesor es
	// mejor camino que el padre
	private boolean mejorCaminoA(NodoA otro, int cero_uno) {

		int indice;

		List<NodoA> list;

		if (cero_uno == 1) {
			list = new ArrayList<>(abiertos); // ArrayList nos deja tratar la p.queue como si fuese una lista
			indice = list.indexOf(otro);

			if (indice != -1) { // Devuelve -1 si no encuentra el elemento 'otro'
				if (otro.getG() < list.get(indice).getG()) { // Comparamos sus costes
					return true;
				} else {
					return false;
				}
			} else { // En caso de que no lo haya encontrado, es el mejor camino
				return true;
			}

		} else {
			if (cerrados.get(otro.getPos()) != null) {
				if (otro.getG() < cerrados.get(otro.getPos())) { // Comparamos sus costes
					return true;
				} else {
					return false;
				}
			} else { // En caso de que no lo haya encontrado, es el mejor camino
				return true;
			}
		}
	}


	private void actualizarAbiertos(NodoA otro) {

		List<NodoA> list = new ArrayList<>(abiertos);
		int indice = list.indexOf(otro);

		abiertos.remove(list.get(indice));
		abiertos.add(otro);

	}

	private void actualizarCerrados(NodoA otro) {

		cerrados.remove(otro);
		abiertos.add(otro);

	}

	// Método para comprobar si una casilla es accesible por el avatar. Comprueba
	// 1. Que la casilla esté dentro de las dimensiones del grid
	// 2. Que la casilla no sea ni muro ni trampa
	private boolean puedoMover(Coordenadas casilla) {

		boolean obstaculo = false, esta_dentro = false;

		esta_dentro = casilla.x >= 0 && casilla.x < (mygrid.length) && casilla.y >= 0 && casilla.y < (mygrid[0].length);

		if (obstaculos.contains(casilla)) {
			obstaculo = true;
		}

		return !obstaculo && esta_dentro;
	}

	public int dMan(CoordenadasLLave nodo, Vector2d meta) {
		return (int) (Math.abs(nodo.x - meta.x) + Math.abs(nodo.y - meta.y));
	}

	// Funcion del profe Guillermo, convierte la orientación a acción
	public static ACTIONS orientationToAction(Vector2d orientation) {
		if (orientation.x == 1)
			return ACTIONS.ACTION_RIGHT;
		if (orientation.x == -1)
			return ACTIONS.ACTION_LEFT;
		if (orientation.y == 1)
			return ACTIONS.ACTION_DOWN;
		if (orientation.y == -1)
			return ACTIONS.ACTION_UP;
		return ACTIONS.ACTION_NIL;
	}
}
