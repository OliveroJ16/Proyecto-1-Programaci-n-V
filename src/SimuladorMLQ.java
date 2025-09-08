import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class SimuladorMLQ {

    private final int quantum;
    private final int cambioContexto;
    private int numeroProcesos;
    private ColaProcesos colaProcesos; // Simular para una sola cola (debe ser una cola)
    private Proceso procesoEnEjecucion;
    private int tiempoAcual;
    private int tiempoRestanteQuantum;
    private boolean enCambioContexto;

     // Atributos para metricas
    private int tiempoTotalEjecucion;       
    private int tiempoTotalCambioContexto;  
    private int tiempoTotalBloqueo;     
    private int tiempoTotalEspera; //Tiempo total de procesos en cola?
    
    //Contructor
    public SimuladorMLQ(int cambioContexto, int quantum, int numeroProcesos) {
        this.quantum = quantum;
        this.cambioContexto = cambioContexto;
        this.numeroProcesos = numeroProcesos;
        this.colaProcesos = new ColaProcesos(0); //Tomando en cuenta que es solo para una "Cola 0"
        this.procesoEnEjecucion = null;
        this.tiempoAcual = 0;
        this.tiempoRestanteQuantum = 0;
        this.enCambioContexto = false;
        this.tiempoTotalEjecucion = 0;
        this.tiempoTotalCambioContexto = 0;
        this.tiempoTotalBloqueo = 0;
        this.tiempoTotalEspera = 0;
    }

    // Método para crear e inicializar los procesos
    //public 


    // Crear los procesos en la cola
    public void crearProcesos(){
        for(int i = 0; i < numeroProcesos; i++){
            Proceso proceso = new Proceso(i, this.colaProcesos.getIdCola(), new Random().nextInt(2), new Random().nextBoolean());
            colaProcesos.agregarProceso(proceso);
        }
    }
}

