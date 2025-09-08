import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class SimuladorMLQ {

    private static final Random random = new Random();
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

    // Crear los procesos en la cola
    public void crearProcesos(){
        for(int i = 0; i < numeroProcesos; i++){
            boolean requiereBloqueo = random.nextBoolean();
            
            Proceso proceso = new Proceso(
                i,
                this.colaProcesos.getIdCola(),
                random.nextInt(96) + 5, // 5 a 100 instrucciones
                requiereBloqueo
            );
            colaProcesos.agregarProceso(proceso);
        }
    }

    public List<String> ejecutarSimulacion() {
        List<String> infoActual = new ArrayList<>();

        while (!simulacionCompleta()) {
            infoActual.add("Tiempo actual: " + tiempoAcual);
            colaProcesos.procesarBloqueados();

            if (enCambioContexto) {
                enCambioContexto = false;
                tiempoTotalCambioContexto++;
            } else {
                if (procesoEnEjecucion == null && !colaProcesos.procesosListosVacia()) {
                    procesoEnEjecucion = colaProcesos.obtenerProceso();
                    procesoEnEjecucion.ejecutar();
                    tiempoRestanteQuantum = quantum;
                }
                if (procesoEnEjecucion != null) {
                    ejecutarProceso();
                }
            }
            tiempoAcual ++;
            actualizarTiempos();
            infoActual.add(mostrarTablaEstado());
        }
        return infoActual;
    }

    private void actualizarTiempos() {
        for (Proceso proceso : colaProcesos.getProcesosActuales()) {
            // Actualizar tiempo en cola solo si esta en estado listo
            if (proceso.getEstado() == EstadoProceso.Listo && proceso != procesoEnEjecucion) {
                proceso.incrementarTiempoEnCola();
            }
        }
    }

    private boolean simulacionCompleta() {
        return colaProcesos.getProcesosTerminados().size() == numeroProcesos;
    }

    public void ejecutarProceso(){
        if (procesoEnEjecucion == null) return;

        procesoEnEjecucion.incrementarTiempoEjecucion();

        if (procesoEnEjecucion.isRequiereBloqueo() && procesoEnEjecucion.getTiempoEjecucion() >= 1) {
            procesoEnEjecucion.setRequerirBloqueo(false);
            procesoEnEjecucion.incrementarTiempoCambioContexto();
            colaProcesos.bloquearProceso(procesoEnEjecucion, random.nextInt(7) + 2);
            enCambioContexto = true;
            procesoEnEjecucion = null;
            return;
        }

        procesoEnEjecucion.ejecutarInstruccion();
        tiempoRestanteQuantum --;
        tiempoTotalEjecucion --;

        if (procesoEnEjecucion.getCantidadInstrucciones() <= 0) {
            colaProcesos.terminarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
        }else if (tiempoRestanteQuantum <= 0) {
            procesoEnEjecucion.listo();
            procesoEnEjecucion.incrementarTiempoCambioContexto();
            colaProcesos.reinsertarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
        }
    }

    private String mostrarTablaEstado() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("%-4s %-5s %-9s %-5s %-3s %-7s %-5s %-5s%n", 
            "Proc", "Inst.", "Estado", "Cola", "CDC", "Bloqueo", "Exe", "Time"));

        for (Proceso proceso : colaProcesos.getProcesosActuales()) {
            sb.append(String.format("P%-4d %-5d %-9s %-5d %-3d %-7d %-5d %-5d%n",
                proceso.getIdProceso(),
                proceso.getCantidadInstrucciones(),
                proceso.getEstado(),
                proceso.getTiempoEnCola(),
                proceso.getTiempoCambioContexto(),
                proceso.getTiempoBloqueado(),
                proceso.getTiempoEjecucion(),
                proceso.getTiempoTotal()
            ));
        }

        sb.append("------------------------------------------------\n");
        return sb.toString();
    }


    public List<Proceso> obtenerProcesos() {
        return colaProcesos.getProcesosActuales();
    }
}

