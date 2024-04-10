package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.Objects;

import ontology.Types;
import ontology.Types.ACTIONS;

class Nodo implements Comparable<Nodo> {
	
	protected Nodo padre;				//Padre del nodo
	protected Types.ACTIONS accion;		//Accion que hizo falta para llegar al nodo
    protected Coordenadas pos;				//Posicion del nodo
    protected Integer coste;			//Coste

    // Constructor
    public Nodo(Nodo padre, Types.ACTIONS accion, Coordenadas pos, Integer coste){
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
    
    public Nodo[] expandir(){
    	
    	 Nodo aux[] = new Nodo[4];

    	
		aux[0] = (new Nodo(this, ACTIONS.ACTION_UP, new Coordenadas(pos.x, pos.y-1), this.getCoste() + 1));	//Nodo arriba
    	
		aux[1] = (new Nodo(this, ACTIONS.ACTION_DOWN, new Coordenadas(pos.x, pos.y+1), this.getCoste() + 1));	//Nodo abajo
    	
		aux[2] = (new Nodo(this, ACTIONS.ACTION_LEFT, new Coordenadas(pos.x-1, pos.y), this.getCoste() + 1));	//Nodo izqda
    	
		aux[3] = (new Nodo(this, ACTIONS.ACTION_RIGHT, new Coordenadas(pos.x+1, pos.y), this.getCoste() + 1));	//Nodo dcha
    			

    	return aux;
    }
    	
        
    public Nodo getPadre() {
        return padre;
    }

    public Coordenadas getPos() {
        return pos;
    }
    
    public ACTIONS getAccion() {
        return accion;
    }
    
    public void setPos(Coordenadas pos) {
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

    @Override
    public boolean equals(Object o){
        Nodo otroNodo = (Nodo) o;
        return (this.pos.x == otroNodo.pos.x && this.pos.y == otroNodo.pos.y);
    }


}