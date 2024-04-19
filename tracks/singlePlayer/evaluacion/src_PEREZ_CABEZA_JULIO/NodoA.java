package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.Objects;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;

class NodoA implements Comparable<NodoA> {
	
	protected NodoA padre;				//Padre del nodo
	protected Types.ACTIONS accion;		//Accion que hizo falta para llegar al nodo
    protected CoordenadasLLave pos;		//Posicion del nodo
    protected Integer f;				//Coste = g+h, siendo h el valor heurístico para llegar a este nodo
    protected Integer g;

    // Constructor
    public NodoA(NodoA padre, Types.ACTIONS accion, CoordenadasLLave pos, Integer g, Integer h){
        this.padre = padre;
        this.accion = accion;
        this.pos = pos;
        this.f = h + g;
        this.g = g;
    }
    
    public NodoA(NodoA otro){
        this.padre = otro.getPadre();
        this.accion = otro.getAccion();
        this.pos = otro.getPos();
        this.f = otro.f;
        this.g = otro.g;
    }
    
    public NodoA [] expandir(Vector2d portal){
    	NodoA aux[] = new NodoA[4];
    	CoordenadasLLave pos_aux;
    	switch(accion) {
    		case ACTION_UP:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new NodoA(this, ACTIONS.ACTION_UP, pos_aux, this.getG() + 1, dMan(pos_aux, portal));
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new NodoA(this, ACTIONS.ACTION_DOWN, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new NodoA(this, ACTIONS.ACTION_LEFT, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new NodoA(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    			break;
    			
    		case ACTION_DOWN:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new NodoA(this, ACTIONS.ACTION_UP, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new NodoA(this, ACTIONS.ACTION_DOWN, pos_aux, this.getG() + 1, dMan(pos_aux, portal));
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new NodoA(this, ACTIONS.ACTION_LEFT, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new NodoA(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    			break;
    			
    		case ACTION_LEFT:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new NodoA(this, ACTIONS.ACTION_UP, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new NodoA(this, ACTIONS.ACTION_DOWN, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new NodoA(this, ACTIONS.ACTION_LEFT, pos_aux, this.getG() + 1, dMan(pos_aux, portal));
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new NodoA(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    			break;
    			
    		case ACTION_RIGHT:
    			// Arriba
    			pos_aux = new CoordenadasLLave(pos.x, pos.y-1, ACTIONS.ACTION_UP);
    	        aux[0] = new NodoA(this, ACTIONS.ACTION_UP, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Abajo
    	        pos_aux = new CoordenadasLLave(pos.x, pos.y+1, ACTIONS.ACTION_DOWN);
    	        aux[1] = new NodoA(this, ACTIONS.ACTION_DOWN, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Izquierda
    	        pos_aux = new CoordenadasLLave(pos.x-1, pos.y, ACTIONS.ACTION_LEFT);
    	        aux[2] = new NodoA(this, ACTIONS.ACTION_LEFT, pos_aux, this.getG() + 2, dMan(pos_aux, portal));
    	        
    	        // Derecha
    	        pos_aux = new CoordenadasLLave(pos.x+1, pos.y, ACTIONS.ACTION_RIGHT);
    	        aux[3] = new NodoA(this, ACTIONS.ACTION_RIGHT, pos_aux, this.getG() + 1, dMan(pos_aux, portal));
    			break;
		default:	
			break;
    	}
    	
        return aux;
    }
    	
        
    public NodoA getPadre() {
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

    public Integer getF() {
        return f;
    }
    
    public Integer getG() {
    	return g;
    }
    
    public void setF(Integer coste) {
    	this.f = coste;
    }
    
    public int dMan(CoordenadasLLave nodo, Vector2d meta) {
    	return (int) (Math.abs(nodo.x - meta.x) + Math.abs(nodo.y - meta.y));
    }


    @Override
    public int compareTo(NodoA otroNodo) {
    	
    	if(this.getF() == otroNodo.getF()) {
    		return (this.getG() - otroNodo.getG());
    	}
        //positivo implica otro mejor que actual, negativo lo contrario
        return (this.getF() - otroNodo.getF());
    }
    
    @Override
    public boolean equals(Object o){
        NodoA otroNodo = (NodoA) o;
        return (this.pos.x == otroNodo.pos.x && this.pos.y == otroNodo.pos.y && this.getAccion() == otroNodo.getAccion() );
    }

}