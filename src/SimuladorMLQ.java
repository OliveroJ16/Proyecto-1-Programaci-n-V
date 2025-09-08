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
            boolean requiereBloqueo = siRequiereBloqueo();
            int tiempoBloqueado = generarTiempoBloqueo(requiereBloqueo);
            Proceso proceso = new Proceso(i, this.colaProcesos.getIdCola(), generarNumeroInstrucciones(),requiereBloqueo, tiempoBloqueado);
            colaProcesos.agregarProceso(proceso);
        }
    }

    public void mostrarTablaProceso(){
        System.out.printf("%-4s %-4s %-5s %-6s %-3s %-7s %-5s%n", 
            "Proc", "Cola", "Inst.", "Estado", "CDC", "Bloqueo", "TiempoBlock");
        
        for(Proceso proceso : colaProcesos.getProcesosActuales()){
            String bloqueo = proceso.isRequiereBloqueo() ? "Si" : "No";
            System.out.printf("P%-4d %-4d %-5d %-6s %-3d %-7s %-5d%n",
                proceso.getIdProceso(),
                proceso.getIdCola(),
                proceso.getCantidadInstrucciones(),
                proceso.getEstado(),
                proceso.getTiempoCambioContexto(),
                bloqueo,
                proceso.getTiempoBloqueado()
            );
        }
        System.out.println();
    }

    private int generarNumeroInstrucciones(){
        return new Random().nextInt(96) + 5; // Tiempo de instrucciones entre 5 y 100
    }

    private boolean siRequiereBloqueo(){
        return new Random().nextBoolean();
    }

    private int generarTiempoBloqueo(boolean requiereBloqueo){
        return requiereBloqueo ?  new Random().nextInt(7) + 2 : 0; // Tiempo de bloqueo entre 2 y 8
    }
}

