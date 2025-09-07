import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AdministradorMLQ {
    
    private int cambioContexto;
    private Queue<Cola> colas;
    private int quantum;
    private int procesosPorCola;

     // Atributos para metricas
    private int tiempoTotalEjecucion;        // desde inicio hasta fin de la simulación
    private int tiempoTotalCambioContexto;   // suma de todos los cambios de contexto
    private int tiempoTotalBloqueo;          // suma de todos los tiempos en bloqueados
    private int tiempoTotalEspera;           // suma de todos los tiempos que procesos estuvieron en cola de listos
    private List<Proceso> procesosBloqueados; // para guardar los procesos bloqueados
    

    //Contructor
    public AdministradorMLQ(int cambioContexto, int quantum, int procesosPorCola) {
        this.cambioContexto = cambioContexto;
        this.quantum = quantum;
        this.procesosPorCola = procesosPorCola;
        this.colas = new LinkedList<>();
        this.tiempoTotalEjecucion = 0;
        this.tiempoTotalCambioContexto = 0;
        this.tiempoTotalBloqueo = 0;
        this.tiempoTotalEspera = 0;
        this.procesosBloqueados = new LinkedList<>();
    }

    // Método para crear e inicializar la lista de colas
    public Queue<Cola> crearColas(int numeroColas) {
        for (int i = 0; i < numeroColas; i++) {
            Cola cola = new Cola(i);
            this.colas.add(cola);
        }
        return this.colas;
    }

    // Crear los procesos en cada N cola
    public void crearProcesosEnColas(){
        for(Cola cola: this.colas){
            cola.crearProcesos(procesosPorCola);
        }
    }

    //Metodo para procesar los procesos (borrador)
    public Proceso procesarProcesos(Proceso proceso) {
        proceso.setEstado(EstadoProceso.Ejecucion);
        int quantumProceso = this.quantum;
        
        if (proceso.getRequireBloqueo()) {
            // Ejecuta 1 instrucción antes de bloquear
            proceso.disminuirTiempoInstrucciones();
            proceso.aumentarTiempoEjecucion();
            this.tiempoTotalEjecucion++;

            // Simula bloqueo
            proceso.simularBloqueado();
            proceso.setRequireBloqueo(false);
            this.procesosBloqueados.add(proceso);
            proceso.setEstado(EstadoProceso.Bloqueado);

        } else {
            // Ejecuta hasta que acabe instrucciones o consuma quantum
            while (proceso.getNumeroInstrucciones() > 0 && quantumProceso > 0) {
                proceso.disminuirTiempoInstrucciones();
                proceso.aumentarTiempoEjecucion();
                this.tiempoTotalEjecucion++;
                quantumProceso--;

            }

            // Decidir estado final
            if (proceso.getNumeroInstrucciones() <= 0) {
                proceso.setEstado(EstadoProceso.Terminado);
            } else {
                proceso.setEstado(EstadoProceso.Listo);
            }
        }

        // Cada proceso conlleva un cambio de contexto
        this.tiempoTotalCambioContexto++;
        this.tiempoTotalEjecucion++;

        return proceso;
    }


    //Getter y Setter
    public int getCambioContexto() {
        return this.cambioContexto;
    }

    public Queue<Cola> getColas() {
        return this.colas;
    }

    public int getQuantum() {
        return this.quantum;
    }

    public int getProcesosPorCola() {
        return this.procesosPorCola;
}
}

