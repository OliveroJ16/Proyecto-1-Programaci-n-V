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

    // Crear procesos en 2 colas (soporta número impar)
    public void crearProcesos() {
        int procesosAsignados = 0;

        for (int i = 0; i < 2; i++) {
            ColaProcesos cola = new ColaProcesos(i + 1);

            // Cola 1 recibe numeroProcesos / 2
            // Cola 2 recibe lo que falte
            int procesosEnCola = (i == 1) ? (numeroProcesos - procesosAsignados) : (numeroProcesos / 2);

            for (int j = 0; j < procesosEnCola; j++) {
                int idProceso = procesosAsignados++;
                boolean requiereBloqueo = random.nextBoolean();

                Proceso proceso = new Proceso(
                        idProceso,
                        cola.getIdCola(),
                        random.nextInt(96) + 5, // 5 a 100 instrucciones
                        requiereBloqueo
                );

                cola.agregarProceso(proceso);
            }
            this.colas.add(cola);
        }
    }

    // Round Robin entre colas
    private ColaProcesos obtenerSiguienteCola() {
        for (int i = 0; i < colas.size(); i++) {
            ColaProcesos cola = colas.get(colaActual);
            if (!cola.procesosListosVacia()) {
                colaActual = (colaActual + 1) % colas.size();
                return cola;
            }
            colaActual = (colaActual + 1) % colas.size();
        }
        return null;
    }

    public List<String> ejecutarSimulacion() {
        List<String> infoActual = new ArrayList<>();

        while (!simulacionCompleta()) {
            infoActual.add("Tiempo actual: " + tiempoAcual);

            for (ColaProcesos cola : colas) {
                cola.procesarBloqueados();
            }

            if (enCambioContexto) {
                enCambioContexto = false;
                tiempoTotalCambioContexto++;
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
        }
        return infoActual;
    }

    private void actualizarTiempos() {
        for (ColaProcesos cola : colas) {
            for (Proceso proceso : cola.getProcesosActuales()) {
                if (proceso.getEstado() == EstadoProceso.Listo && proceso != procesoEnEjecucion) {
                    proceso.incrementarTiempoEnCola();
                    tiempoTotalEspera++;
                }
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

        ColaProcesos colaDelProceso = null;
        for (ColaProcesos cola : colas) {
            if (cola.getProcesosActuales().contains(procesoEnEjecucion)) {
                colaDelProceso = cola;
                break;
            }
        }
        if (colaDelProceso == null) return;

        // Ejecutar ciclo
        procesoEnEjecucion.ejecutar();
        procesoEnEjecucion.ejecutarInstruccion();
        procesoEnEjecucion.incrementarTiempoEjecucion();
        tiempoTotalEjecucion++;
        tiempoRestanteQuantum--;

        // --- TERMINAR ---
        if (procesoEnEjecucion.getCantidadInstrucciones() <= 0) {
            if (!procesoEnEjecucion.isMarcarParaTerminar()) {
                procesoEnEjecucion.setMarcarParaTerminar(true); // mostrar una vez en Ejecución
            } else {
                colaDelProceso.terminarProceso(procesoEnEjecucion);
                procesoEnEjecucion = null;
                enCambioContexto = true;
                return;
            }
        }

        // --- BLOQUEO ---
        if (procesoEnEjecucion != null && procesoEnEjecucion.isRequiereBloqueo()) {
            if (!procesoEnEjecucion.isYaEjecutadoPrimeraVez()) {
                procesoEnEjecucion.setYaEjecutadoPrimeraVez(true);
            } else {
                procesoEnEjecucion.setRequerirBloqueo(false);
                // ✅ Asignamos tiempo de bloqueo +1 para compensar la resta inmediata
                procesoEnEjecucion.asignarTiempoBloqueado(procesoEnEjecucion.getTiempoBloqueoDefinido() + 1);
                procesoEnEjecucion.incrementarTiempoCambioContexto(); // CDC al bloquear
                colaDelProceso.bloquearProceso(procesoEnEjecucion);
                enCambioContexto = true;
                procesoEnEjecucion = null;
                return;
            }
        }

        // --- QUANTUM AGOTADO ---
        if (procesoEnEjecucion != null && tiempoRestanteQuantum <= 0) {
            procesoEnEjecucion.listo();
            procesoEnEjecucion.incrementarTiempoCambioContexto(); // CDC al reinsertar
            colaDelProceso.reinsertarProceso(procesoEnEjecucion);
            procesoEnEjecucion = null;
            enCambioContexto = true;
        }
    }

    private String mostrarTablaEstado() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("%-4s %-5s %-6s %-5s %-9s %-3s %-7s %-5s %-5s%n",
                "Proc", "IdCola", "Cola", "Inst.", "Estado", "CDC", "Bloqueo", "Exe", "Time"));
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
                        proceso.getTiempoBloqueado(),
                        proceso.getTiempoEjecucion(),
                        proceso.getTiempoTotal()));
            }
            contadorCola++;
            if (contadorCola < colas.size()) {
                sb.append("----------------------------------------------------------\n");
            }
        }

        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("Proceso en ejecución: %s%n",
                procesoEnEjecucion != null ? "P" + procesoEnEjecucion.getIdProceso() : "Ninguno"));

        // 🔹 eliminar procesos terminados después de mostrarlos una vez
        for (ColaProcesos cola : colas) {
            cola.getProcesosActuales().removeIf(p -> p.getEstado() == EstadoProceso.Terminado);
        }

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
}
