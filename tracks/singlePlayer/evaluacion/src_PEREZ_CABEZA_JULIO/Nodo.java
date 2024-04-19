package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.Objects;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;

class Nodo implements Comparable<Nodo> {
	
	protected Nodo padre;				//Padre del nodo
	protected Types.ACTIONS accion;		//Accion que hizo falta para llegar al nodo
    protected CoordenadasLLave pos;				//pos del nodo
    protected Integer coste;			//Coste

    // Constructor
    public Nodo(Nodo padre, Types.ACTIONS accion, CoordenadasLLave pos, Integer coste){
        this.padre = padre;
        this.accion = accion;
        this.pos = pos;
        this.coste = coste;
    }
    
    public Nodo(Nodo otro){
        this.padre = otro.getPadre();
        this.accion = otro.getAccion();
        this.pos = otro.getPos();
        this.coste = otro.coste;
    }
    
    public Nodo [] expandir(){
    	Nodo aux[] = new Nodo[4];
    	CoordenadasLLave pos_aux;
    	switch(accion) {
    		case ACTION_UP:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new Nodo(this, ACTIONS.ACTION_UP, pos_aux, this.getCoste() + 1);
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new Nodo(this, ACTIONS.ACTION_DOWN, pos_aux, this.getCoste() + 2);
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new Nodo(this, ACTIONS.ACTION_LEFT, pos_aux, this.getCoste() + 2);
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new Nodo(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getCoste() + 2);
    			break;
    			
    		case ACTION_DOWN:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new Nodo(this, ACTIONS.ACTION_UP, pos_aux, this.getCoste() + 2);
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new Nodo(this, ACTIONS.ACTION_DOWN, pos_aux, this.getCoste() + 1);
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new Nodo(this, ACTIONS.ACTION_LEFT, pos_aux, this.getCoste() + 2);
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new Nodo(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getCoste() + 2);
    			break;
    			
    		case ACTION_LEFT:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new Nodo(this, ACTIONS.ACTION_UP, pos_aux, this.getCoste() + 2);
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new Nodo(this, ACTIONS.ACTION_DOWN, pos_aux, this.getCoste() + 2);
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new Nodo(this, ACTIONS.ACTION_LEFT, pos_aux, this.getCoste() + 1);
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new Nodo(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getCoste() + 2);
    			break;
    			
    		case ACTION_RIGHT:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new Nodo(this, ACTIONS.ACTION_UP, pos_aux, this.getCoste() + 2);
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new Nodo(this, ACTIONS.ACTION_DOWN, pos_aux, this.getCoste() + 2);
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new Nodo(this, ACTIONS.ACTION_LEFT, pos_aux, this.getCoste() + 2);
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new Nodo(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getCoste() + 1);
    			break;
		default:	
			break;
    	}
    	
        return aux;
    }
    	
        
    public Nodo getPadre() {
        return padre;
    }

    public CoordenadasLLave getPos() {
        return pos;
    }
    
    public ACTIONS getAccion() {
        return accion;
    }
    
    public void setPos(CoordenadasLLave pos) {
    	this.pos = pos;
    }

    public Integer getCoste() {
        return coste;
    }
    
    public void setCoste(Integer coste) {
    	this.coste = coste;
    }


    @Override
    public int compareTo(Nodo otroNodo) {
        // Comparamos los costes de los nodos (en caso de que sea negativo el mejor coste es el del nodo actual (si es 0 son iguales en coste))
        return (this.getCoste() - otroNodo.getCoste());
    }


}