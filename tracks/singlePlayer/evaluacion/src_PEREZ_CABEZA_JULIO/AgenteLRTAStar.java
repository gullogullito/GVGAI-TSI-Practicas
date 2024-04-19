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

public class AgenteLRTAStar extends AbstractPlayer {

	Vector2d fescala;
	Vector2d portal;

	PriorityQueue<NodoA> abiertos; // Nodos abiertos
	HashMap<Coordenadas, Integer> visitados; // Nodos ya visitados, los queremos guardar para ir cambiando su H
	Types.ACTIONS accion; // Accion a ejecutar, dictada por el metodo RTAStar()
	boolean tengoRuta; // 0 si no hay ruta planeada, 1 caso contrario
	ArrayList<Observation>[][] mygrid; // Grid
	NodoA meta;
	HashSet<Coordenadas> obstaculos;
	Integer nNodos;
	Boolean girando, termino;
	long tiempo ;
	ArrayList<Observation> muros, trampas; 

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteLRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
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

		accion = ACTIONS.ACTION_RIGHT;
		visitados = new HashMap<Coordenadas, Integer>();
		mygrid = stateObs.getObservationGrid();
		girando = false;
		nNodos = 0;
		termino = false;
		tiempo = 0;
		
		muros = stateObs.getImmovablePositions()[0];
		trampas = new ArrayList<Observation>();
		if( stateObs.getImmovablePositions().length > 1)
			trampas = stateObs.getImmovablePositions()[1];
		obstaculos = new HashSet<Coordenadas>();
		
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
	}

	/**
	 * return the best action to arrive faster to the closest portal
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return best ACTION to arrive faster to the closest portal
	 */
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		if( stateObs.getImmovablePositions().length > 0 && stateObs.getImmovablePositions().length > 1) {
			if(trampas.size() != stateObs.getImmovablePositions()[1].size() || muros.size() != stateObs.getImmovablePositions()[0].size()) {
				actualizarMapa(stateObs);
			}
		}
				
		if(girando) {
			nNodos++;
			girando = false;
			return accion;
		}
		
		long tInicio = System.nanoTime();
		
		RTAStar(stateObs);
		
		long tFin = System.nanoTime();
		
		long tiempoTotalms = (tFin - tInicio) / 1000000;
		
		tiempo += tiempoTotalms;
		
		if(termino) {
			System.out.print("\nRuntime acumulado :  ");System.out.print(tiempo);
			System.out.print("\nTamaño de la ruta :  ");System.out.print(nNodos);
			System.out.print("\nNodos expandidos :  ");System.out.print(nNodos);
			System.out.print("\n\n");
		}

		return accion;

	}

	// Método que implementa
	private void RTAStar(StateObservation stateObs) {

		// Inicialmente calculamos la posicion del avatar
		Coordenadas pos = new Coordenadas(stateObs.getAvatarPosition().x / fescala.x,
				stateObs.getAvatarPosition().y / fescala.y);
		NodoRTA actual;
		
		if(!visitados.containsKey(pos)) {
			actual = new NodoRTA( orientationToAction(stateObs.getAvatarOrientation()), pos, 0,
					dMan(pos, portal));
		}else {
			actual = new NodoRTA( orientationToAction(stateObs.getAvatarOrientation()), pos, 0, visitados.get(pos));
		}
		
		
		//Creamos el espacio de búsqueda, creando los 4 vecinos y añadiendo solo si no son obstaculo
		NodoRTA expandidos[] = actual.expandir(portal, visitados);
		PriorityQueue<NodoRTA> esp_busqueda = new PriorityQueue<NodoRTA>() ;

		for (int i = 0; i < 4 ; i ++) {
			if (puedoMover(expandidos[i].getPos())) {
				if (expandidos[i].getAccion() != actual.getAccion()) {
					expandidos[i].setG(2);
				}
				esp_busqueda.add(expandidos[i]);
			}
		}
		
		//Cogemos el mejor vecino
		accion = esp_busqueda.peek().getAccion();
		if(esp_busqueda.peek().getPos().x == portal.x  && esp_busqueda.peek().getPos().y == portal.y) {
			termino = true;
		}
		
		//Aprendemos nueva H
		int nueva_h = setNewH(actual, esp_busqueda);
		actual.setH(nueva_h);
		
		//Añadimos a visitados el nodo actual, con su nueva H
		visitados.put(actual.getPos(), actual.getH());
		nNodos++;
		
		//Añadimos dos 
		if(accion != actual.getAccion()) {
			girando = true ;
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
	
	public int setNewH(NodoRTA actual, PriorityQueue<NodoRTA> vecinos) {
		NodoRTA vecino2;
		
		vecino2 = vecinos.poll(); //Cogemos el (primer) mínimo
		
		return Math.max((int)(actual.getH()), (int)(vecino2.getF()));
	}

	public int dMan(Coordenadas nodo, Vector2d meta) {
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
	
	public void actualizarMapa(StateObservation stateObs) {
		mygrid = stateObs.getObservationGrid();
		
		obstaculos.clear();
		
		muros = stateObs.getImmovablePositions()[0];
		trampas = new ArrayList<Observation>();
		if( stateObs.getImmovablePositions().length > 1)
			trampas = stateObs.getImmovablePositions()[1];
		obstaculos = new HashSet<Coordenadas>();
		
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
	}
}
