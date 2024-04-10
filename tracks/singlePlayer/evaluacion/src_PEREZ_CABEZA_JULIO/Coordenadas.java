package tracks.singlePlayer.evaluacion.src_PEREZ_CABEZA_JULIO;

import java.util.Objects;

import tools.Vector2d;

public class Coordenadas extends Vector2d{

	Coordenadas(){
		super();	//Heredamos de Vector 2d
	}
	
	Coordenadas(Coordenadas otro){
		super(otro);
	}
	
	Coordenadas(double x, double y){
		super(x,y);
	}

	public int hashCode() {
		return Objects.hash(x,y);
	}
}
