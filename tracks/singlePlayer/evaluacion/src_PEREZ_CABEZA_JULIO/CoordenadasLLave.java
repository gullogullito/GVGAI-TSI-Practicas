package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.Objects;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class CoordenadasLLave extends Vector2d{

	protected Types.ACTIONS accion;
	
	CoordenadasLLave(){
		super();	//Heredamos de Vector 2d
		accion = ACTIONS.ACTION_NIL;
	}
	
	CoordenadasLLave(CoordenadasLLave otro){
		super(otro);
		this.accion = otro.accion;
	}
	
	CoordenadasLLave(double x, double y, Types.ACTIONS accion){
		super(x,y);
		this.accion = accion;
	}

	public int hashCode() {
		return Objects.hash(x,y,accion);
	}
	
	public Coordenadas getCoordenadas() {
		return new Coordenadas(super.x, super.y);
	}
}
