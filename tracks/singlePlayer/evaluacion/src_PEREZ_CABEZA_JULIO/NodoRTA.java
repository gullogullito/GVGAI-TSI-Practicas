package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.HashMap;
import java.util.Objects;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;

class NodoRTA implements Comparable<NodoRTA> {
	
	protected Types.ACTIONS accion;		//Accion que hizo falta para llegar al nodo
    protected Coordenadas pos;				//Posicion del nodo
    protected Integer f;			//Coste = g+h, siendo h el valor heurístico para llegar a este nodo
    protected Integer g;
    protected Integer h;

    // Constructor
    public NodoRTA( Types.ACTIONS accion, Coordenadas pos, Integer g, Integer h){
        this.accion = accion;
        this.pos = pos;
        this.f = h + g;
        this.g = g;
        this.h = h;
    }
    
    public NodoRTA(NodoRTA otro){
        this.accion = otro.getAccion();
        this.pos = otro.getPos();
        this.f = otro.f;
        this.g = otro.g;
        this.h = otro.h;
    }
    
    public NodoRTA[] expandir(Vector2d meta, HashMap<Coordenadas, Integer> visitados){
    	
    	NodoRTA aux[] = new NodoRTA[4];
    	Coordenadas pos_aux;
    	Integer nueva_h;

		pos_aux = new Coordenadas(pos.x, pos.y-1);
		if(!visitados.containsKey(pos_aux)) {
			nueva_h = dMan(pos_aux,meta);
		}else {
			nueva_h = visitados.get(pos_aux);
		}
		aux[0] = (new NodoRTA( ACTIONS.ACTION_UP, pos_aux , this.getG() + 1, nueva_h));	//Nodo arriba
		
		
		pos_aux = new Coordenadas(pos.x, pos.y+1);
		if(!visitados.containsKey(pos_aux)) {
			nueva_h = dMan(pos_aux,meta);
		}else {
			nueva_h = visitados.get(pos_aux);
		}
		aux[1] = (new NodoRTA( ACTIONS.ACTION_DOWN, pos_aux , this.getG() + 1, nueva_h));	//Nodo abajo
		
		
		pos_aux = new Coordenadas(pos.x-1, pos.y);
		if(!visitados.containsKey(pos_aux)) {
			nueva_h = dMan(pos_aux,meta);
		}else {
			nueva_h = visitados.get(pos_aux);
		}
		aux[2] = (new NodoRTA( ACTIONS.ACTION_LEFT, pos_aux , this.getG() + 1, nueva_h));	//Nodo izqda
		
		
		pos_aux = new Coordenadas(pos.x+1, pos.y);
		if(!visitados.containsKey(pos_aux)) {
			nueva_h = dMan(pos_aux,meta);
		}else {
			nueva_h = visitados.get(pos_aux);
		}
		aux[3] = (new NodoRTA( ACTIONS.ACTION_RIGHT, pos_aux , this.getG() + 1, nueva_h));	//Nodo dcha
		
    	return aux;
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

    public Integer getF() {
        return f;
    }
    
    public Integer getG() {
    	return g;
    }
    
    public Integer getH() {
    	return h;
    }
    public void setH(Integer h) {
    	this.h = h;
    	this.f = g+h;
    }
    
    public void setG(Integer g) {
    	this.g = g;
    	this.f = g+h;
    }
    
    public void setF(Integer coste) {
    	this.f = coste;
    }
    
    public int dMan(Coordenadas nodo, Vector2d meta) {
    	return (int) (Math.abs(nodo.x - meta.x) + Math.abs(nodo.y - meta.y));
    }


    @Override
    public int compareTo(NodoRTA otroNodo) {
    	if(this.getF() == otroNodo.getF()) {
    		return (this.getG() - otroNodo.getG());
    	}
        // Comparamos las dos heurísticas de los nodos
        return (this.getF() - otroNodo.getF());
    }

    @Override
    public boolean equals(Object o){
        NodoA otroNodo = (NodoA) o;
        return (this.pos.x == otroNodo.pos.x && this.pos.y == otroNodo.pos.y );
    }

}