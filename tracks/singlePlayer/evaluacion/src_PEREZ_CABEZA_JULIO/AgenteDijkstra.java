package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

public class AgenteDijkstra extends AbstractPlayer {

	Vector2d fescala;
	Vector2d portal;

	PriorityQueue<Nodo> abiertos; 					// Nodos abiertos
	HashMap<CoordenadasLLave, Integer> visitados; 	// Nodos ya visitados
	Stack<Types.ACTIONS> ruta; 						// Pila con la ruta, siempre sacamos la accion mas longeva
	boolean tengoRuta; 								// 0 si no hay ruta planeada, 1 caso contrario
	ArrayList<Observation>[][] mygrid; 				// Grid de todas las observaciones
	Nodo meta;
	int nNodos;
	ACTIONS primeraAccion;
	HashSet<Coordenadas> obstaculos; 				//Aquí se van a guardar todos los muros y trampas
	ArrayList<Observation> muros, trampas;

	
	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteDijkstra(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// Calculamos el factor de escala entre mundos
		fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
				stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);

		// Se crea una lista de observaciones de portales, ordenada por cercania al
		// avatar
		ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
		// Seleccionamos el portal mas proximo
		portal = posiciones[0].get(0).position;
		portal.x = Math.floor(portal.x / fescala.x);
		portal.y = Math.floor(portal.y / fescala.y);

		ruta = new Stack<>();									//Pila que va a contener las acciones calculadas
		abiertos = new PriorityQueue<Nodo>();					//Guardamos abiertos en p_queue, ordenados por su coste = g
		visitados = new HashMap<CoordenadasLLave, Integer>();	//Los nodos visitados tienen por clave (Posicion, Accion) y por valor su coste
		tengoRuta = false;										
		mygrid = stateObs.getObservationGrid();					//Grid con todas las observaciones
		
		
		//Para gestionar el tema de las casillas no transitables, creamos muros y trampas
		//Los vamos a guardar en un Set, de manera que su acceso sea O(1)
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
		
		//Primera acción
		primeraAccion = orientationToAction(stateObs.getAvatarOrientation());
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

			CosteUniforme(stateObs);

			long tFin = System.nanoTime();

			long tiempoTotalms = (tFin - tInicio) / 1000000;

			System.out.print("\nRuntime acumulado :  ");System.out.print(tiempoTotalms);
			
			if (ruta.isEmpty()) {
				//He encontrado la meta, guardando sus mejores padres
				Nodo aux = meta;
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

	// Método que implementa Dijkstra
	private void CosteUniforme(StateObservation stateObs) {

		// Inicialmente calculamos la posicion del avatar, creamos el nodo padre, que es
		// la casilla y posicion de salida (con
		// coste 0) y lo añadimos a los nodos abiertos
		CoordenadasLLave pos = new CoordenadasLLave(stateObs.getAvatarPosition().x / fescala.x,
				stateObs.getAvatarPosition().y / fescala.y, orientationToAction(stateObs.getAvatarOrientation()));
		Nodo inicial = new Nodo(null, orientationToAction(stateObs.getAvatarOrientation()), pos, 0);
		Nodo expandidos[];
		abiertos.add(inicial);
		Nodo sucesor;
		int dist;

		while (!abiertos.isEmpty() && !tengoRuta) { // Condición de parada: Si he encontrado ruta o si me quedo sin
													// nodos abiertos

			// Sacamos en primer lugar el nodo con menor coste
			Nodo actual = abiertos.poll();

			// Si encuentro un nodo con posición de la meta, termino el algoritmo
			if (actual.pos.x == portal.x && actual.pos.y == portal.y) {
				
				meta = actual;
				tengoRuta = true;
				
			} else {

				if (!visitados.containsKey(actual.getPos())) {	//Si ya lo hemos visitado pasamos
					
					// Añadimos a visitados o explorados el nodo actual y expandimos sus 4 nodos
					// posibles
					visitados.put(actual.getPos(), actual.getCoste());
					expandidos = actual.expandir();
					
					//Por cada nodo que añado a visitados, aumentamos el contador de nodos expandidos
					nNodos++;
					
					// Para todos los sucesores, comprobamos que no ha sido visitado anteriormente
					for (int i = 0; i < expandidos.length; i++) {
						sucesor = expandidos[i];

						//Si el sucesor no está en visitados y es válido, vamos a añadirlo a abiertos
						if (!visitados.containsKey(sucesor.getPos()) && puedoMover(sucesor.getPos().getCoordenadas())) {
							
							//1 si son misma accion 2 si son distintos
							dist = distancia(actual, sucesor);
							
							//Actualizamos su coste en caso de encontrar un mejor camino
							if (sucesor.getCoste() > actual.getCoste() + dist) {
								sucesor.setCoste(actual.getCoste() + dist);
							}

							abiertos.add(sucesor);
						}
					}
				}
			}
		}
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

	//Calculo la distancia del actual al sucesor: 1 si son la misma accion y 2 (rotar y mover) si son distintas acciones
	private int distancia(Nodo actual, Nodo sucesor) {

		if (actual.getAccion() == sucesor.getAccion()) {
			return 1;
		}

		return 2;
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