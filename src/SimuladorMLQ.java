import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimuladorMLQ {

    private static final Random random = new Random();
    private final int quantum;
    private final int cambioContexto;
    private int numeroProcesos;
    private List<ColaProcesos> colas;
    private Proceso procesoEnEjecucion;
    private int tiempoAcual;
    private int tiempoRestanteQuantum;
    private boolean enCambioContexto;
    private int colaActual;

    // Métricas
    private int tiempoTotalEjecucion;
    private int tiempoTotalCambioContexto;
    private int tiempoTotalBloqueo;
    private int tiempoTotalEspera;

    public SimuladorMLQ(int cambioContexto, int quantum, int numeroProcesos) {
        this.quantum = quantum;
        this.cambioContexto = cambioContexto;
        this.numeroProcesos = numeroProcesos;
        this.colas = new ArrayList<>();
        this.procesoEnEjecucion = null;
        this.tiempoAcual = 0;
        this.tiempoRestanteQuantum = 0;
        this.enCambioContexto = false;
        this.colaActual = 0;
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
                int idProceso = i * (numeroProcesos / 2) + j;
                boolean requiereBloqueo = random.nextBoolean();

                Proceso proceso = new Proceso(idProceso, cola.getIdCola(), random.nextInt(10) + 2, requiereBloqueo);

                if (requiereBloqueo) {
                    proceso.asignarTiempoBloqueado(random.nextInt(2) + 3);
                }
                cola.agregarProceso(proceso);
            }
            this.colas.add(cola);
        }
    }

    // Round Robin entre colas
    private ColaProcesos obtenerSiguienteCola() {
        ColaProcesos cola = colas.get(colaActual);
        colaActual = (colaActual + 1) % 2; // Alternae entre 0 y 1

        /*
         * Si la cola tiene procesos listos, la devolvemos
         * si no, revisamos la otra cola
         */
        if (!cola.procesosListosVacia()) {
            return cola;
        } else {
            // Revisamos la otra cola
            ColaProcesos cola2 = colas.get(colaActual);
            return cola2.procesosListosVacia() ? cola2 : null;
        }
    }

    /*
     * metodo para iniciar la simulacion
     */
    public List<String> ejecutarSimulacion() {
        List<String> infoActual = new ArrayList<>(); // Arraylist para guardar cada iteracion de los procesos (logs)

        /*
         * ciclo para para marcar los proceos como listos
         */
        for (ColaProcesos cola : colas) {
            for (Proceso proceso : cola.getProcesosActuales()) {
                proceso.listo();
            }
        }

        while (!simulacionCompleta()) {
            infoActual.add("Tiempo actual: " + tiempoAcual);

            for (ColaProcesos cola : colas) {
                cola.procesarBloqueados();
            }

            if (enCambioContexto) {
                enCambioContexto = false;
            } else {
                if (procesoEnEjecucion == null) {
                    ColaProcesos colaSeleccionada = obtenerSiguienteCola();
                    if (colaSeleccionada != null) {
                        procesoEnEjecucion = colaSeleccionada.obtenerProceso();
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
            limpiarProcesosTerminados(); //Elimina los procesos marcados como terminado para el logs
        }
        calcularMetricas();
        return infoActual;
    }

    private void actualizarTiempos() {
        for (ColaProcesos cola : colas) {
            for (Proceso proceso : cola.getProcesosActuales()) {
                if (proceso.getEstado() == EstadoProceso.Listo && proceso != procesoEnEjecucion) {
                    proceso.incrementarTiempoEnCola();
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

    private void aplicarCambioContexto(Proceso proceso) {
        if (proceso != null) {
            proceso.incrementarTiempoCambioContexto();
        }
    }

    public void ejecutarProceso() {
        if (procesoEnEjecucion == null)
            return;

        ColaProcesos colaDelProceso = null;
        for (ColaProcesos cola : colas) {
            if (cola.getProcesosActuales().contains(procesoEnEjecucion)) {
                colaDelProceso = cola;
                break;
            }
        }
        if (colaDelProceso == null)
            return;

        // Ejecutar proceso y controlar cambio de contexto
        if (procesoEnEjecucion.getSiCambioContexto()) {
            procesoEnEjecucion.incrementarTiempoCambioContexto();
            procesoEnEjecucion.listo();
            procesoEnEjecucion.setSiCambioContexto(false);
        } else{
            procesoEnEjecucion.ejecutar();
            procesoEnEjecucion.ejecutarInstruccion();
            procesoEnEjecucion.incrementarTiempoEjecucion();
        }

        tiempoRestanteQuantum--;

        // Bloqueo
        if (procesoEnEjecucion.isRequiereBloqueo() &&
                procesoEnEjecucion.getTiempoEjecucion() > 1) {
            procesoEnEjecucion.setRequerirBloqueo(false);
            colaDelProceso.bloquearProceso(procesoEnEjecucion);
            aplicarCambioContexto(procesoEnEjecucion);
            enCambioContexto = true;
            procesoEnEjecucion = null;
            return;
        }

        // Terminó
        if (procesoEnEjecucion.getCantidadInstrucciones() <= 0) {
            colaDelProceso.terminarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
        }
        // Quantum agotado
        else if (tiempoRestanteQuantum < 0) {
            procesoEnEjecucion.listo();
            procesoEnEjecucion.setSiCambioContexto(true);
            colaDelProceso.reinsertarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
        }
    }

    private void limpiarProcesosTerminados() {
        for (ColaProcesos cola : colas) {
            cola.getProcesosActuales().removeIf(Proceso::isMostrarTerminado);
        }
    }

    private String mostrarTablaEstado() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("%-4s %-5s %-6s %-5s %-9s %-3s %-7s %-5s %-5s%n",
                "Proc", "IdCola", "EnCola", "Inst.", "Estado", "CDC", "Bloqueo", "Exe", "Time"));
        sb.append("----------------------------------------------------------\n");

        int contadorCola = 0;
        for (ColaProcesos cola : colas) {
            for (Proceso proceso : cola.getProcesosActuales()) {
                sb.append(String.format("P%-4d %-5d %-6d %-5d %-9s %-3d %-7d %-5d %-5d%n",
                        proceso.getIdProceso(),
                        proceso.getIdCola(),
                        proceso.getTiempoEnCola(),
                        proceso.getCantidadInstrucciones(),
                        proceso.getEstado(),
                        proceso.getTiempoCambioContexto(),
                        proceso.getContBloqueo(),
                        proceso.getTiempoEjecucion(),
                        proceso.getTiempoTotal()));
            }
            contadorCola++;
            if (contadorCola < colas.size()) {
                sb.append("----------------------------------------------------------\n");
            }
        }
        sb.append("----------------------------------------------------------\n");
        return sb.toString();
    }

    public List<Proceso> obtenerProcesos() {
        List<Proceso> todosProcesos = new ArrayList<>();
        for (ColaProcesos cola : colas) {
            todosProcesos.addAll(cola.getProcesosActuales());
        }
        return todosProcesos;
    }

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

    private void calcularMetricas() {
        for (ColaProcesos colaProcesos : colas) {
            for (Proceso p : colaProcesos.getProcesosTerminados()) {
                tiempoTotalEjecucion = tiempoTotalEjecucion + p.getTiempoEjecucion();
                tiempoTotalCambioContexto = tiempoTotalCambioContexto + p.getTiempoCambioContexto();
                tiempoTotalBloqueo = tiempoTotalBloqueo + p.getContBloqueo();
                tiempoTotalEspera = tiempoTotalEspera + p.getTiempoEnCola();
            }
        }
    }
}
