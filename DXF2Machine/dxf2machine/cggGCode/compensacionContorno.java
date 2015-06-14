/*------------------------------------------------------------------------------------------ 
Copyright 2014, Celeste Gabriela Guagliano. 

This file is part of DXF2Machine project. 

DXF2Machine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License. 

DXF2Machine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. 

You should have received a copy of the GNU General Public License along with DXF2Machine. If not, see <http://www.gnu.org/licenses/>.
  --------------------------------------------------------------------------------------------*/


package cggGCode;

import java.util.Enumeration;
import java.util.Hashtable;

import cggColeccion.ColeccionFunciones;
import cggDatos.Coordenadas;
import cggDatos.DatosArcos;
import cggDatos.DatosCirculo;
import cggDatos.DatosLinea;
import cggDatos.EcuacionCircunferencia;
import cggDatos.EcuacionEntidad;
import cggDatos.EcuacionRecta;
import cggDatos.FormatoNumeros;
import cggDatos.Herramienta;
import cggDatos.datos;
import cggOptimizacion.OptimizacionMetodo2;

public class compensacionContorno {
	
	public static EcuacionRecta calcularEcuacionRecta(datos datosLinea, double herramienta) {
		
		double A=0;
		double B=0;
		double C=0;
		double pendiente=0;
		double terminoIndependiente=0;
		Coordenadas iniciales=ColeccionFunciones.ObtenerCoordenadaInicioEntidad(datosLinea);
		Coordenadas finales=ColeccionFunciones.ObtenerCoordenadaFinEntidad(datosLinea);
		Coordenadas punto=new Coordenadas(0,0);
		
		double deltaX=(finales.x-iniciales.x);
		double deltaY=(finales.y-iniciales.y);
		if(deltaX!=0){
		B=1;
		pendiente=deltaY/deltaX;
		A=-pendiente;
		C=pendiente*iniciales.x-iniciales.y;
		}else{
		B=0;
		A=-1;
		C=iniciales.x;
		}
		EcuacionRecta original=new EcuacionRecta(A,B,C);
		punto=datosLinea.ObtenerPuntoSobreRectaCompensada(herramienta,original);
		EcuacionRecta compensada=new EcuacionRecta(0,0,0);
		compensada=calcularRectaParalela(original,punto);
		return compensada;
	}

		private static EcuacionRecta calcularRectaParalela(EcuacionRecta original,
		Coordenadas punto) {
	    EcuacionRecta compensada= new EcuacionRecta(original.A,original.B,0);
	    compensada.C=-compensada.A*punto.x-original.B*punto.y;
	 	return compensada;
}

		

		public static EcuacionCircunferencia calcularEcuacionCircunferencia(datos datosCirculo, double radioHerramienta) {
			EcuacionCircunferencia ecuacion=new EcuacionCircunferencia(0,0,0);
			if(datosCirculo instanceof DatosCirculo){
				DatosCirculo dato=(DatosCirculo)datosCirculo;
				ecuacion.centroX=dato.CentroX;
				ecuacion.centroY=dato.CentroY;
				if(dato.orientacion==0){
					ecuacion.Radio=dato.Radio-radioHerramienta;
				}else{
					ecuacion.Radio=dato.Radio+radioHerramienta;
				}
			}else{
				DatosArcos datos=(DatosArcos)datosCirculo;
				ecuacion.centroX=datos.Xcentro;
				ecuacion.centroY=datos.Ycentro;
				if(datos.orientacion==0){
					ecuacion.Radio=datos.radio-radioHerramienta;
				}else{
					ecuacion.Radio=datos.radio+radioHerramienta;
				}
			}
		return ecuacion;
	}

	public static Hashtable intersectarEcuacionesCompensadas(
			Hashtable listaOptimizada, Hashtable listaEcuaciones) {
		Hashtable listaCompensada=new Hashtable();
		int j=0;
		for(int i=1;i<=listaEcuaciones.size();i++){
			EcuacionEntidad ecuacion1=(EcuacionEntidad)listaEcuaciones.get(i);
			EcuacionEntidad ecuacion2=null;
			datos elemento1=(datos)listaOptimizada.get(i);
			datos elemento2=null;
			if(i<listaEcuaciones.size()){
				j=i+1;
				ecuacion2=(EcuacionEntidad)listaEcuaciones.get(j);
				elemento2=(datos)listaOptimizada.get(j);
			}else{
				j=1;
				ecuacion2=(EcuacionEntidad)listaEcuaciones.get(j);
				elemento2=(datos)listaOptimizada.get(j);
			}
			if(elemento1!=elemento2){
			listaCompensada=OptimizacionMetodo2.intersectarEntidades(i,j,ecuacion1,elemento1,ecuacion2,elemento2,listaCompensada);
			}else{
				listaCompensada=compensarCircunferencia(i,(EcuacionCircunferencia)ecuacion1,(DatosCirculo)elemento1);
			}
		}
	
		return listaCompensada;
	}

	private static Hashtable compensarCircunferencia(int i,
			EcuacionCircunferencia ecuacion1, DatosCirculo elemento1) {
		Hashtable listaCompensada=new Hashtable();
		elemento1.ComienzoX=(double) FormatoNumeros.formatearNumero(elemento1.CentroX-ecuacion1.Radio);
		elemento1.FinalX=(double) FormatoNumeros.formatearNumero(elemento1.CentroX+ecuacion1.Radio);
		listaCompensada.put(i, elemento1);
		return listaCompensada;
	}

	public static Coordenadas intersectarRectas(EcuacionRecta ecuacion1,
			EcuacionRecta ecuacion2) {
		Coordenadas interseccion=new Coordenadas(0,0);
		if(ecuacion1.B!=0){
			if(ecuacion2.B!=0){
				interseccion.x=(ecuacion1.C/ecuacion1.B-ecuacion2.C/ecuacion2.B)/(ecuacion2.A/ecuacion2.B-ecuacion1.A/ecuacion1.B);
				interseccion.y=-ecuacion1.A/ecuacion1.B*interseccion.x-ecuacion1.C/ecuacion1.B;
			}else{
				interseccion.x=-ecuacion2.C/ecuacion2.A;
				interseccion.y=-ecuacion1.A/ecuacion1.B*interseccion.x-ecuacion1.C/ecuacion1.B;
			}
		}else{
			interseccion.x=-ecuacion1.C/ecuacion1.A;
			interseccion.y=-ecuacion2.A/ecuacion2.B*interseccion.x-ecuacion2.C/ecuacion2.B;			
		}
		interseccion.x=(double) FormatoNumeros.formatearNumero(interseccion.x);
		interseccion.y=(double) FormatoNumeros.formatearNumero(interseccion.y);
		return interseccion;
	}

	public static Coordenadas intersectarArcoYRecta(EcuacionRecta ecuacion1,
			EcuacionCircunferencia ecuacion2, DatosArcos elemento2) {
		Coordenadas interseccion=new Coordenadas(0,0);
		Coordenadas finArco=ColeccionFunciones.ObtenerCoordenadaFinEntidad(elemento2);
		if(ecuacion1.A==0){
			interseccion=intersectarArcoConRectaHorizontal(ecuacion1,ecuacion2,finArco);
		}else if(ecuacion1.B==0){
			interseccion=intersectarArcoConRectaVertical(ecuacion1,ecuacion2,finArco);
		}else{
			interseccion=intersectarRectaConArcoCasoGeneral(ecuacion1,ecuacion2,finArco);
		}
		return interseccion;
	}

	public static Coordenadas intersectarRectaYArco(EcuacionRecta ecuacion1,
	    EcuacionCircunferencia ecuacion2, DatosArcos elemento2) {
		Coordenadas interseccion=new Coordenadas(0,0);
		Coordenadas inicioArco=ColeccionFunciones.ObtenerCoordenadaInicioEntidad(elemento2);
		if(ecuacion1.A==0){
			interseccion=intersectarArcoConRectaHorizontal(ecuacion1,ecuacion2,inicioArco);
		}else if(ecuacion1.B==0){
			interseccion=intersectarArcoConRectaVertical(ecuacion1,ecuacion2,inicioArco);
		}else{
			interseccion=intersectarRectaConArcoCasoGeneral(ecuacion1,ecuacion2,inicioArco);
		}
		return interseccion;
		}
	
	
		private static Coordenadas intersectarRectaConArcoCasoGeneral(
			EcuacionRecta ecuacion1, EcuacionCircunferencia ecuacion2,
			Coordenadas interseccionArcoOriginal) {
		Coordenadas interseccion=new Coordenadas(0,0);
		double a=(ecuacion1.B*ecuacion1.B)/(ecuacion1.A*ecuacion1.A)+1;
		double b=(2*ecuacion1.B/ecuacion1.A*(ecuacion1.C/ecuacion1.A+ecuacion2.centroX)-2*ecuacion2.centroY);
		double c=Math.pow(ecuacion1.C/ecuacion1.A+ecuacion2.centroX,2)+ecuacion2.centroY*ecuacion2.centroY-ecuacion2.Radio*ecuacion2.Radio;
		double y1=(-b+Math.sqrt(b*b-4*a*c))/2*a;
		double y2=(-b-Math.sqrt(b*b-4*a*c))/2*a;
		interseccion.y=seleccionarPuntoY(ecuacion2,y1,y2,interseccionArcoOriginal);
		interseccion.x=(-ecuacion1.C-ecuacion1.B*interseccion.y)/ecuacion1.A;
		interseccion.x=(double) FormatoNumeros.formatearNumero(interseccion.x);
		interseccion.y=(double) FormatoNumeros.formatearNumero(interseccion.y);
		return interseccion;
	}

		private static Coordenadas intersectarArcoConRectaVertical(
			EcuacionRecta ecuacion1, EcuacionCircunferencia ecuacion2,
			Coordenadas interseccionArcoOriginal) {
		Coordenadas interseccion=new Coordenadas(0,0);
		interseccion.x=-ecuacion1.C/ecuacion1.A;
		double parametro=Math.sqrt(ecuacion2.Radio*ecuacion2.Radio-Math.pow(interseccion.x-ecuacion2.centroX,2));
		if(Math.abs(parametro)==0){
			parametro=0;
		}
		double y1=ecuacion2.centroY+parametro;
		double y2=ecuacion2.centroY-parametro;
		interseccion.y=seleccionarPuntoY(ecuacion2,y1,y2,interseccionArcoOriginal);
		interseccion.x=(double) FormatoNumeros.formatearNumero(interseccion.x);
		interseccion.y=(double) FormatoNumeros.formatearNumero(interseccion.y);
		return interseccion;
	}

		private static double seleccionarPuntoY(
				EcuacionCircunferencia ecuacion2, double y1, double y2,
				Coordenadas interseccionArcoOriginal) {
			double y=0;
			if(interseccionArcoOriginal.y<ecuacion2.centroY){
				if(y1<ecuacion2.centroY){
					y=y1;
				}else{
					y=y2;
				}
			}else{
				if(y1>ecuacion2.centroY){
					y=y1;
				}else{
					y=y2;
				}
			}
			return y;
		}

		private static Coordenadas intersectarArcoConRectaHorizontal(
			EcuacionRecta ecuacion1, EcuacionCircunferencia ecuacion2,
			Coordenadas interseccionArcoOriginal) {
		Coordenadas interseccion=new Coordenadas(0,0);
		interseccion.y=-ecuacion1.C/ecuacion1.B;
		double parametro=0;
		if(FormatoNumeros.formatearNumero(ecuacion2.Radio*ecuacion2.Radio)!=FormatoNumeros.formatearNumero(Math.pow(interseccion.y-ecuacion2.centroY,2))){
		parametro=Math.sqrt(ecuacion2.Radio*ecuacion2.Radio-Math.pow(interseccion.y-ecuacion2.centroY,2));
		}
		double x1=ecuacion2.centroX+parametro;
		double x2=ecuacion2.centroX-parametro;
		interseccion.x=seleccionarPuntoX(ecuacion2,x1,x2,interseccionArcoOriginal);
		interseccion.x=(double) FormatoNumeros.formatearNumero(interseccion.x);
		interseccion.y=(double) FormatoNumeros.formatearNumero(interseccion.y);
		return interseccion;
	}

		private static double seleccionarPuntoX(
				EcuacionCircunferencia ecuacion2, double x1, double x2,
				Coordenadas interseccionArcoOriginal) {
			double x=0;
			if(interseccionArcoOriginal.x<ecuacion2.centroX){
				if(x1<ecuacion2.centroX){
					x=x1;
				}else{
					x=x2;
				}
			}else{
				if(x1>ecuacion2.centroX){
					x=x1;
				}else{
					x=x2;
				}
			}
			return x;
		}

	
	public static Coordenadas intersectarArcos(EcuacionCircunferencia ecuacion1,
			EcuacionCircunferencia ecuacion2, DatosArcos elemento1, DatosArcos elemento2) {
		Coordenadas interseccion=new Coordenadas(0,0);
		double factor=1;
		Coordenadas FinArco= ColeccionFunciones.ObtenerCoordenadaFinEntidad(elemento2);
		if(FinArco.y<((DatosArcos)elemento2).Ycentro){
			factor=-1;
		}
		double A=2*(ecuacion1.centroX-ecuacion2.centroX);
		double B=2*(ecuacion1.centroY-ecuacion2.centroY);
		double C=ecuacion2.centroX-ecuacion1.centroX+ecuacion2.centroY-ecuacion2.centroY-(Math.pow(ecuacion2.Radio, 2)-Math.pow(ecuacion1.Radio, 2));
		double aa=Math.pow(A/B, 2)+1;
		double bb=-2*A*C/(Math.pow(B, 2)*ecuacion2.Radio);
		double cc=(C*C/(B*B*ecuacion2.Radio))-1;
		if(Math.pow(bb, 2)!=4*aa*cc){
			double coseno1=-(bb+Math.sqrt(Math.pow(bb,2)-4*aa*cc))/2*aa;
			double coseno2=-(bb-Math.sqrt(Math.pow(bb,2)-4*aa*cc))/2*aa;
			double ang1=Math.acos(coseno1);
			double ang2=Math.acos(coseno2);
			double angI=elemento2.obtenerAnguloInicial();
			if(Math.abs(angI-ang1)<Math.abs(angI-ang2)){
				interseccion.x=ecuacion2.Radio*coseno1+ecuacion2.centroX;
				interseccion.y=factor*ecuacion2.Radio*Math.sqrt(1-Math.pow((interseccion.x-ecuacion2.centroX)/ecuacion2.Radio, 2))+elemento2.Ycentro;
			}else{
				interseccion.x=ecuacion2.Radio*coseno2+ecuacion2.centroX;
				interseccion.y=factor*ecuacion2.Radio*Math.sqrt(1-Math.pow((interseccion.x-ecuacion2.centroX)/ecuacion2.Radio, 2))+elemento2.Ycentro;
			}
			
		}else{
			double coseno=-bb;
			interseccion.x=ecuacion2.Radio*coseno+ecuacion2.centroX;
			interseccion.y=factor*ecuacion2.Radio*Math.sqrt(1-Math.pow((interseccion.x-ecuacion2.centroX)/ecuacion2.Radio, 2))+elemento2.Ycentro;
		}
		
		interseccion.x=(double) FormatoNumeros.formatearNumero(interseccion.x);
		interseccion.y=(double) FormatoNumeros.formatearNumero(interseccion.y);
		return interseccion;
	}

	public static Hashtable ObtenerListaEcuaciones(
		Hashtable listaEntidadesCompensadas, double radioHerramienta) {
		Hashtable listaEcuaciones=new Hashtable();
		EcuacionEntidad ecuacion= new EcuacionEntidad();
		for(int i=1; i<listaEntidadesCompensadas.size()+1;i++){
			datos elemento=(datos)listaEntidadesCompensadas.get(i);
			ecuacion=elemento.calculaTuEcuacion(radioHerramienta);
			listaEcuaciones.put(i, ecuacion);
		}
		return listaEcuaciones;
	}

}



	
