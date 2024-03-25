package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class AgenteDijkstra extends AbstractPlayer {
	
	//Greedy Camel: 
		// 1) Busca la puerta m�s cercana. 
		// 2) Escoge la accion que minimiza la distancia del camello a la puerta.

		Vector2d fescala;
		Vector2d portal;
		
		PriorityQueue<Nodo> sucesores;		//Nodos abiertos y no visitados
		ArrayList<Nodo> visitados;
		Stack<Types.ACTIONS> ruta ;			//Pila con la ruta, siempre sacamos la accion mas longeva
		boolean tengoRuta;					//0 si no hay ruta planeada, 1 caso contrario
		ArrayList<Observation>[][] mygrid;	//Grid
		
		/**
		 * initialize all variables for the agent
		 * @param stateObs Observation of the current state.
	     * @param elapsedTimer Timer when the action returned is due.
		 */
		public AgenteDijkstra(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
			//Calculamos el factor de escala entre mundos (pixeles -> grid)
	        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
	        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);      
	      
	        //Se crea una lista de observaciones de portales, ordenada por cercania al avatar
	        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
	        //Seleccionamos el portal mas proximo
	        portal = posiciones[0].get(0).position;
	        portal.x = Math.floor(portal.x / fescala.x);
	        portal.y = Math.floor(portal.y / fescala.y);
	        
	        ruta = new Stack<>();
	        sucesores = new PriorityQueue<>();
	        visitados = new ArrayList<>();
	        tengoRuta = false;
	        mygrid = stateObs.getObservationGrid();
		}
		
		/**
		 * return the best action to arrive faster to the closest portal
		 * @param stateObs Observation of the current state.
	     * @param elapsedTimer Timer when the action returned is due.
		 * @return best	ACTION to arrive faster to the closest portal
		 */
		@Override
		public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
	        //Posicion del avatar
	        Vector2d avatar =  new Vector2d(stateObs.getAvatarPosition().x / fescala.x, 
	        		stateObs.getAvatarPosition().y / fescala.y);
	        
	        //Probamos las cuatro acciones y calculamos la distancia del nuevo estado al portal.
	        Vector2d newPos_up = avatar, newPos_down = avatar, newPos_left = avatar, newPos_right = avatar;
	        if (avatar.y - 1 >= 0) {
	        	newPos_up = new Vector2d(avatar.x, avatar.y-1);
	        }
	        if (avatar.y + 1 <= stateObs.getObservationGrid()[0].length-1) {
	        	newPos_down = new Vector2d(avatar.x, avatar.y+1);
	        }
	        if (avatar.x - 1 >= 0) {
	        	newPos_left = new Vector2d(avatar.x - 1, avatar.y);
	        }
	        if (avatar.x + 1 <= stateObs.getObservationGrid().length - 1) {
	        	newPos_right = new Vector2d(avatar.x + 1, avatar.y);
	        }
	        
	        //Manhattan distance
	        ArrayList<Integer> distances = new ArrayList<Integer>();
	        distances.add((int) (Math.abs(newPos_up.x - portal.x) + Math.abs(newPos_up.y-portal.y)));
	        distances.add((int) (Math.abs(newPos_down.x - portal.x) + Math.abs(newPos_down.y-portal.y)));
	        distances.add((int) (Math.abs(newPos_left.x - portal.x) + Math.abs(newPos_left.y-portal.y)));
	        distances.add((int) (Math.abs(newPos_right.x - portal.x) + Math.abs(newPos_right.y-portal.y)));      
	       
	        // Nos quedamos con el menor y tomamos esa accion. 
	        int minIndex = distances.indexOf(Collections.min(distances));
	        switch (minIndex) {
	        	case 0:  
	        		return Types.ACTIONS.ACTION_UP;
	        	case 1:  
	        		return Types.ACTIONS.ACTION_DOWN;
	        	case 2:  
	        		return Types.ACTIONS.ACTION_LEFT;
	        	case 3:  
	        		return Types.ACTIONS.ACTION_RIGHT;
	        	default:
	        		return Types.ACTIONS.ACTION_NIL;
	        }              
			
		}

		private void CosteUniforme(Nodo inicial, StateObservation stateObs) {
			
			Vector2d pos = new Vector2d(stateObs.getAvatarPosition().x / fescala.x,
					stateObs.getAvatarPosition().y / fescala.y);
			Nodo actual = new Nodo(null, Types.ACTIONS.ACTION_NIL, pos, 0);
			ArrayList<Nodo> expandidos = new ArrayList<>();
			sucesores.add(actual);
						
			while (!tengoRuta) {
				
				actual = sucesores.poll();				
				visitados.add(actual);
				sucesores.remove(actual);
				
				if(actual.getPos() != portal) {
				
					expandidos = actual.expandir(portal);
					
					for( int i = 0; i < expandidos.size(); i++) {
						Nodo sucesor = expandidos.get(i);
						sucesores.add(sucesor);
						Integer distancia;
						
						if (!visitados.contains(sucesor) && actual.getCoste() > actual.getCoste() +  distancia) {
							sucesor.setCoste(actual.getCoste()+ distancia);
						}
						
					}
				}
				
				else
					tengoRuta = true;
			}
		}
		
		private boolean esObstaculo(Vector2d casilla) {
			boolean obstaculo = false;
			
			int i = 0;
			
			
			while ( i < mygrid[(int)casilla.x][(int)casilla.y].size() && !obstaculo) {
	            if(mygrid[(int)casilla.x][(int)casilla.y].get(i).itype == 'w' || mygrid[(int)casilla.x][(int)casilla.y].get(i).itype == 't')
	                obstaculo = true;
	            i++;
			}
		}
}



class Nodo implements Comparable<Nodo> {
	
	private Nodo padre;				//Padre del nodo
	protected ArrayList<Nodo> hijos;//Lista de hijos, pueden ser 4
	private Types.ACTIONS accion;	//Accion que hizo falta para llegar al nodo
    private Vector2d pos;			//Posicion del nodo
    
    private Integer coste;			//Coste 

    // Constructor
    public Nodo(Nodo padre, Types.ACTIONS accion, Vector2d pos, Integer coste){
        this.padre = padre;
        this.accion = accion;
        hijos = new ArrayList<>(4);
        this.pos = pos;
    }
    
    public ArrayList<Nodo> expandir(Vector2d meta){
    	
    	hijos.add(new Nodo(this, ACTIONS.ACTION_UP, new Vector2d(pos.x, pos.y-1), coste + 1));	//Nodo arriba
    	
    	hijos.add(new Nodo(this, ACTIONS.ACTION_DOWN, new Vector2d(pos.x, pos.y+1), coste + 1));	//Nodo abajo
    	
    	hijos.add(new Nodo(this, ACTIONS.ACTION_LEFT, new Vector2d(pos.x-1, pos.y), coste + 1));	//Nodo izqda
    	
    	hijos.add(new Nodo(this, ACTIONS.ACTION_RIGHT, new Vector2d(pos.x+1, pos.y), coste + 1));	//Nodo dcha
    	
    	
    	return hijos;
    }
    	
        
    public Nodo getPadre() {
        return padre;
    }
    
    public ArrayList<Nodo> getHijos() {
        return hijos;
    }

    public Vector2d getPos() {
        return pos;
    }
    
    public void setPos(Vector2d pos) {
    	this.pos = pos;
    }

    public Integer getCoste() {
        return coste;
    }
    
    public void setCoste(Integer coste) {
    	this.coste = coste;
    }
    
    
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


	@Override
	public int compareTo(Nodo o) {
		// TODO Auto-generated method stub
		return this.coste > o.coste ? 1 : -1;		//Orden descendiente
	}
}


