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
    private List<ColaProcesos> colas; // Lista de colas en lugar de una sola cola ya que el orden no importa
    private Proceso procesoEnEjecucion;
    private int tiempoAcual;
    private int tiempoRestanteQuantum;
    private boolean enCambioContexto;
    private int colaActual; // Índice de la cola actual para intercalar

    // Atributos para métricas
    private int tiempoTotalEjecucion;
    private int tiempoTotalCambioContexto;
    private int tiempoTotalBloqueo;
    private int tiempoTotalEspera; // Tiempo total de procesos en cola

    // Constructor
    public SimuladorMLQ(int cambioContexto, int quantum, int numeroProcesos) {
        this.quantum = quantum;
        this.cambioContexto = cambioContexto;
        this.numeroProcesos = numeroProcesos;
        this.colas = new ArrayList<>(); // Inicializar como ArrayList
        this.procesoEnEjecucion = null;
        this.tiempoAcual = 0;
        this.tiempoRestanteQuantum = 0;
        this.enCambioContexto = false;
        this.colaActual = 0; // Empezar con la cola 0
        this.tiempoTotalEjecucion = 0;
        this.tiempoTotalCambioContexto = 0;
        this.tiempoTotalBloqueo = 0;
        this.tiempoTotalEspera = 0;
    }

    // Crear los procesos en las colas
    public void crearProcesos() {
        for (int i = 0; i < 2; i++) {
            ColaProcesos cola = new ColaProcesos(i + 1);
            for (int j = 0; j < numeroProcesos / 2; j++) {
                int idProceso = i * (numeroProcesos / 2) + j; // ID único
                boolean requiereBloqueo = random.nextBoolean();

                Proceso proceso = new Proceso(
                        idProceso,
                        cola.getIdCola(),
                        random.nextInt(96) + 5, // 5 a 100 instrucciones
                        requiereBloqueo);
                if (requiereBloqueo) {
                    proceso.asignarTiempoBloqueado(random.nextInt(7) + 2);
                }
                cola.agregarProceso(proceso);
            }
            this.colas.add(cola);
        }
    }

    // Método para intercalar entre colas
    private ColaProcesos obtenerSiguienteCola() {
        // Buscar la siguiente cola con procesos listos
        for (int i = 0; i < colas.size(); i++) {
            ColaProcesos cola = colas.get(colaActual);
            if (!cola.procesosListosVacia()) {
                // Cambiar a la siguiente cola para la próxima vez
                colaActual = (colaActual + 1) % colas.size();
                return cola;
            }
            // Si la cola actual no tiene procesos, probar la siguiente
            colaActual = (colaActual + 1) % colas.size();
        }
        return null; // No hay procesos listos en ninguna cola
    }

    // Ejecutar simulación con intercalado entre colas
    public List<String> ejecutarSimulacion() {
        List<String> infoActual = new ArrayList<>();

        while (!simulacionCompleta()) {
            infoActual.add("Tiempo actual: " + tiempoAcual);

            // Procesar bloqueados en todas las colas
            for (ColaProcesos cola : colas) {
                cola.procesarBloqueados();
            }

            if (enCambioContexto) {
                enCambioContexto = false;
                tiempoTotalCambioContexto++;
            } else {
                // Si no hay proceso en ejecución, obtener uno de forma intercalada
                if (procesoEnEjecucion == null) {
                    ColaProcesos colaSeleccionada = obtenerSiguienteCola();
                    if (colaSeleccionada != null) {
                        procesoEnEjecucion = colaSeleccionada.obtenerProceso();
                        //procesoEnEjecucion.ejecutar();
                        tiempoRestanteQuantum = quantum;
                    }
                }

                if (procesoEnEjecucion != null) {
                    ejecutarProceso();
                }
            }

            tiempoAcual++;
            actualizarTiempos();
            infoActual.add(mostrarTablaEstado());
        }
        return infoActual;
    }

    private void actualizarTiempos() {
        // Actualizar tiempos para todas las colas
        for (ColaProcesos cola : colas) {
            for (Proceso proceso : cola.getProcesosActuales()) {
                // Actualizar tiempo en cola solo si está en estado listo
                if (proceso.getEstado() == EstadoProceso.Listo && proceso != procesoEnEjecucion) {
                    proceso.incrementarTiempoEnCola();
                    tiempoTotalEspera++;
                }
                // Actualizar tiempo de bloqueo para procesos bloqueados
                if (proceso.getEstado() == EstadoProceso.Bloqueado) {
                    tiempoTotalBloqueo++;
                }
            }
        }
    }

    private boolean simulacionCompleta() {
        int totalTerminados = 0;
        for (ColaProcesos cola : colas) {
            totalTerminados += cola.getProcesosTerminados().size();
        }
        return totalTerminados == numeroProcesos;
    }

    public void ejecutarProceso() {
        if (procesoEnEjecucion == null)
            return;

        // Encontrar la cola del proceso en ejecución
        ColaProcesos colaDelProceso = null;
        for (ColaProcesos cola : colas) {
            if (cola.getProcesosActuales().contains(procesoEnEjecucion)) {
                colaDelProceso = cola;
                break;
            }
        }

        if (colaDelProceso == null)
            return;


        //CAMBIOS QUE SE ESTAN REALIZANDO
        if (procesoEnEjecucion.getSiCambioContexto()) {
            procesoEnEjecucion.incrementarTiempoCambioContexto();
            procesoEnEjecucion.listo();
            procesoEnEjecucion.setSiCambioContexto(false);
        }else{ 
            procesoEnEjecucion.ejecutar();
            procesoEnEjecucion.ejecutarInstruccion();
            procesoEnEjecucion.incrementarTiempoEjecucion();
            tiempoTotalEjecucion++;
        }

        // Ejecuta una instrucción en CPU
        //procesoEnEjecucion.ejecutarInstruccion();
        //procesoEnEjecucion.incrementarTiempoEjecucion();
        tiempoRestanteQuantum--;
        //tiempoTotalEjecucion++;

        // --- Verificar BLOQUEO después de ejecutar ---
        if (procesoEnEjecucion.isRequiereBloqueo()) {
            // Si ya ejecutó al menos 1 instrucción -> ahora sí bloquear
            if (procesoEnEjecucion.getTiempoEjecucion() > 1) {
                procesoEnEjecucion.setRequerirBloqueo(false);
                procesoEnEjecucion.incrementarTiempoCambioContexto();
                colaDelProceso.bloquearProceso(procesoEnEjecucion);
                enCambioContexto = true;
                procesoEnEjecucion = null;
                return;
            }
        }

        // --- Proceso terminó ---
        if (procesoEnEjecucion != null && procesoEnEjecucion.getCantidadInstrucciones() <= 0) {
            colaDelProceso.terminarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
        }
        // --- Quantum agotado ---
        else if (procesoEnEjecucion != null && tiempoRestanteQuantum <= 0) {
            procesoEnEjecucion.listo();
            procesoEnEjecucion.incrementarTiempoCambioContexto();
            procesoEnEjecucion.setSiCambioContexto(true);
            colaDelProceso.reinsertarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
            
        }
    }

    private String mostrarTablaEstado() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append(String.format("%-4s %-5s %-6s %-5s %-3s %-7s %-5s %-5s %-5s%n",
            "Proc", "IdCola", "Cola", "Inst.", "Estado", "CDC", "Bloqueo", "Exe", "Time"));
    sb.append("----------------------------------------------------------\n");

    int contadorCola = 0;
    // Mostrar procesos de todas las colas
    for (ColaProcesos cola : colas) {
        for (Proceso proceso : cola.getProcesosActuales()) {
            sb.append(String.format("P%-4d %-5d %-6d %-5d %-9s %-3d %-7d %-5d %-5d%n",
                    proceso.getIdProceso(),
                    proceso.getIdCola(),
                    proceso.getTiempoEnCola(),        // <-- Tiempo en cola agregado
                    proceso.getCantidadInstrucciones(),
                    proceso.getEstado(),
                    proceso.getTiempoCambioContexto(),
                    proceso.getTiempoBloqueado(),
                    proceso.getTiempoEjecucion(),
                    proceso.getTiempoTotal()));
        }

        // Línea separadora entre colas, excepto después de la última
        contadorCola++;
        if (contadorCola < colas.size()) {
            sb.append("----------------------------------------------------------\n");
        }
    }

    sb.append("----------------------------------------------------------\n");
    sb.append(String.format("Proceso en ejecución: %s%n",
            procesoEnEjecucion != null ? "P" + procesoEnEjecucion.getIdProceso() : "Ninguno"));

    return sb.toString();
}

    public List<Proceso> obtenerProcesos() {
        List<Proceso> todosProcesos = new ArrayList<>();
        for (ColaProcesos cola : colas) {
            todosProcesos.addAll(cola.getProcesosActuales());
        }
        return todosProcesos;
    }

    // Método para obtener métricas finales
    public void mostrarMetricas() {
        System.out.println("=== MÉTRICAS FINALES ===");
        System.out.println("Tiempo total de ejecución: " + tiempoTotalEjecucion);
        System.out.println("Tiempo total de cambio de contexto: " + tiempoTotalCambioContexto);
        System.out.println("Tiempo total de bloqueo: " + tiempoTotalBloqueo);
        System.out.println("Tiempo total de espera en cola: " + tiempoTotalEspera);
        System.out.println("Tiempo total de simulación: " + tiempoAcual);

        for (int i = 0; i < colas.size(); i++) {
            ColaProcesos cola = colas.get(i);
            System.out.println("Cola " + (i + 1) + " - Procesos terminados: " +
                    cola.getProcesosTerminados().size());
        }
    }

    // Getters para acceso a las colas individuales
    public ColaProcesos getCola(int indice) {
        if (indice >= 0 && indice < colas.size()) {
            return colas.get(indice);
        }
        return null;
    }

    public List<ColaProcesos> getColas() {
        return colas;
    }
}