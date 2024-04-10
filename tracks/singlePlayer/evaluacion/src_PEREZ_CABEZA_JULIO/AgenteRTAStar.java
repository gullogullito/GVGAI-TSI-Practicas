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

public class AgenteRTAStar extends AbstractPlayer {

	Vector2d fescala;
	Vector2d portal;

	PriorityQueue<NodoA> abiertos; // Nodos abiertos
	HashMap<Coordenadas, Integer> visitados; // Nodos ya visitados, los queremos guardar para ir cambiando su H
	Types.ACTIONS accion; // Accion a ejecutar, dictada por el metodo RTAStar()
	boolean tengoRuta; // 0 si no hay ruta planeada, 1 caso contrario
	ArrayList<Observation>[][] mygrid; // Grid
	NodoA meta;
	ArrayList<Integer>[][] obstaculos; // Matriz de obstaculos: 1 para obstaculo, 0 si no lo es
	Integer coste_actual;
	Boolean girando;

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
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
		abiertos = new PriorityQueue<NodoA>();
		visitados = new HashMap<Coordenadas, Integer>();
		tengoRuta = false;
		mygrid = stateObs.getObservationGrid();
		girando = false;
	}

	/**
	 * return the best action to arrive faster to the closest portal
	 * 
	 * @param stateObs     Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return best ACTION to arrive faster to the closest portal
	 */
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		if(girando) {
			girando = false;
			return accion;
		}
		
		RTAStar(stateObs);

		return accion;

	}

	// Método que implementa A*
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
		
		System.out.print(" \n \n Accion actual ");System.out.print(accion);
		System.out.print("\n");
		
		
		//Creamos el espacio de búsqueda, creando los 4 vecinos y añadiendo solo si no son obstaculo
		NodoRTA expandidos[] = actual.expandir(portal, visitados);
		PriorityQueue<NodoRTA> esp_busqueda = new PriorityQueue<NodoRTA>() ;

		for (int i = 0; i < 4 ; i ++) {
			if (puedoMover(expandidos[i].getPos())) {
				if (expandidos[i].getAccion() != actual.getAccion()) {
					expandidos[i].setG(2);
				}
				esp_busqueda.add(expandidos[i]);
				System.out.print("\n  - Accion expandida:");System.out.print(expandidos[i].getAccion());System.out.print("  G: ");System.out.print(expandidos[i].getG());System.out.print("  H: ");System.out.print(expandidos[i].getH());
				System.out.print("\n");
			}
		}
		
		//Cogemos el mejor vecino
		List<NodoRTA> list =  new ArrayList<>(esp_busqueda);
		accion = list.get(0).getAccion();
		System.out.print("\n     - ACCION elegida: ");System.out.print(accion);System.out.print("\n");
		
		//Aprendemos nueva H
		int nueva_h = setNewH(actual, list);
		actual.setH(nueva_h);
		
		//Añadimos a visitados el nodo actual, con su nueva H
		System.out.print("\n Meto posicion ");System.out.print(actual.getPos());System.out.print(" con nueva H: ");System.out.print(nueva_h);System.out.print("\n");
		visitados.put(actual.getPos(), actual.getH());
		
		//Añadimos dos 
		if(accion != actual.getAccion()) {
			girando = true ;
		}
		
	}

	// Método para comprobar si una casilla es accesible por el avatar. Comprueba
	// 1. Que la casilla esté dentro de las dimensiones del grid
	// 2. Que la casilla no sea ni muro ni trampa
	private boolean puedoMover(Vector2d casilla) {

		boolean obstaculo = false, esta_dentro = false;

		esta_dentro = casilla.x >= 0 && casilla.x < (mygrid.length) && casilla.y >= 0 && casilla.y < (mygrid[0].length);

		int i = 0;

		while (i < mygrid[(int) casilla.x][(int) casilla.y].size() && !obstaculo) {
			if (mygrid[(int) casilla.x][(int) casilla.y].get(i).itype == 0
					|| mygrid[(int) casilla.x][(int) casilla.y].get(i).itype == 8)
				obstaculo = true;
			i++;
		}

		return !obstaculo && esta_dentro;
	}
	
	public int setNewH(NodoRTA actual, List<NodoRTA> vecinos) {
		NodoRTA vecino2;
		
		if(vecinos.size() > 1) {
			vecino2 = vecinos.get(1);	//cogemos el segundo mínimo
		}else if (vecinos.size() == 1){
			vecino2 = vecinos.get(0);	//en caso de que solo haya un vecino cogemos como segundo minimo el mismo
		}else {						
			return (int)(actual.getH());	//en caso de que no haya vecinos nos quedamos con la misma F (aqui nunca se entra)
		}
		
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
}
