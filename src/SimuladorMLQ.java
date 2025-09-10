import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimuladorMLQ {

    private static final Random random = new Random();
    private final int quantum;
    private final int cambioContexto;        // en ms (por especificación: 1 ms)
    private int numeroProcesos;
    private List<ColaProcesos> colas;        // varias colas MLQ
    private Proceso procesoEnEjecucion;
    private int tiempoActual;
    private int tiempoRestanteQuantum;
    private boolean enCambioContexto;
    private int colaActual; // índice para intercalar colas

    // métricas
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
        this.tiempoActual = 0;
        this.tiempoRestanteQuantum = 0;
        this.enCambioContexto = false;
        this.colaActual = 0;
        this.tiempoTotalEjecucion = 0;
        this.tiempoTotalCambioContexto = 0;
        this.tiempoTotalBloqueo = 0;
        this.tiempoTotalEspera = 0;
    }

    // Crear 2 colas y repartir procesos (mitad y mitad)
    public void crearProcesos() {
        // Aseguramos 2 colas (especificación)
        int numColas = 2;
        for (int i = 0; i < numColas; i++) {
            ColaProcesos cola = new ColaProcesos(i + 1);
            this.colas.add(cola);
        }

        // Repartir procesos entre las 2 colas
        int mitad = numeroProcesos / 2;
        int id = 0;
        for (int i = 0; i < numColas; i++) {
            int toCreate = (i == 0) ? mitad + (numeroProcesos % 2) : mitad; // si impar, cola 0 recibe 1 extra
            for (int j = 0; j < toCreate; j++) {
                boolean requiereBloqueo = random.nextBoolean();
                Proceso p = new Proceso(id, colas.get(i).getIdCola(), random.nextInt(96) + 5, requiereBloqueo);
                if (requiereBloqueo) {
                    // asignar tiempo de bloqueo (2-8 ms) al crear
                    p.asignarTiempoBloqueado(random.nextInt(7) + 2);
                }
                colas.get(i).agregarProceso(p);
                id++;
            }
        }
    }

    // busca la siguiente cola con procesosListos (intercalado RR entre colas)
    private ColaProcesos obtenerSiguienteCola() {
        int intentos = 0;
        int start = colaActual;
        while (intentos < colas.size()) {
            ColaProcesos cola = colas.get(colaActual);
            colaActual = (colaActual + 1) % colas.size(); // mueve el puntero para la próxima vez (intercalado)
            if (!cola.procesosListosVacia()) {
                return cola;
            }
            intentos++;
        }
        return null; // ninguna cola tiene procesos listos
    }

    // Aplica el CDC cuando cambiamos de proceso (lo cuenta en el proceso que perdió CPU)
    private void aplicarCambioContexto(Proceso procesoQueSale) {
        if (procesoQueSale != null) {
            procesoQueSale.incrementarTiempoCambioContexto(); // al proceso que pierde CPU
        }
        tiempoActual += cambioContexto;
        tiempoTotalCambioContexto += cambioContexto;
    }

    // Loop principal (por milisegundo)
    public List<String> ejecutarSimulacion() {
        List<String> infoActual = new ArrayList<>();

        while (!simulacionCompleta()) {
            infoActual.add("Tiempo actual: " + tiempoActual);

            // reducir bloqueos en todas las colas (y mover a listo si terminó)
            for (ColaProcesos cola : colas) {
                cola.procesarBloqueados();
            }

            // Si no hay proceso en ejecucion, obtener uno intercalando colas
            if (procesoEnEjecucion == null) {
                ColaProcesos colaSeleccionada = obtenerSiguienteCola();
                if (colaSeleccionada != null) {
                    Proceso nuevo = colaSeleccionada.obtenerProceso();
                    if (nuevo != null) {
                        // Si va a entrar un nuevo proceso y hubo un proceso previo, aplicar CDC
                        aplicarCambioContexto(null); // aplicamos CDC como latencia al asignar CPU (especificación)
                        procesoEnEjecucion = nuevo;
                        procesoEnEjecucion.ejecutar();
                        tiempoRestanteQuantum = quantum;
                    }
                }
            }

            // Ejecutar 1 ms/instrucción del proceso en CPU
            if (procesoEnEjecucion != null) {
                // encontrar la cola del proceso actual
                ColaProcesos colaDelProceso = null;
                for (ColaProcesos cola : colas) {
                    if (cola.getProcesosActuales().contains(procesoEnEjecucion)) {
                        colaDelProceso = cola;
                        break;
                    }
                }

                // Ejecuta 1 instrucción
                procesoEnEjecucion.ejecutarInstruccion();
                procesoEnEjecucion.incrementarTiempoEjecucion();
                tiempoRestanteQuantum--;
                tiempoTotalEjecucion++;
                
                // VERIFICAR BLOQUEO: solo puede bloquearse si requiereBloqueo y ya ejecutó al menos 1 instrucción
                if (procesoEnEjecucion.isRequiereBloqueo() && procesoEnEjecucion.getTiempoEjecucion() >= 1) {
                    // bloquear ahora
                    procesoEnEjecucion.setRequerirBloqueo(false); // evitar re-bloqueos
                    procesoEnEjecucion.incrementarTiempoCambioContexto();
                    // movemos a bloqueados en su cola
                    if (colaDelProceso != null) {
                        colaDelProceso.bloquearProceso(procesoEnEjecucion);
                    }
                    // contabilizar bloqueo (ya el tiempo de bloqueo fue asignado al crear)
                    tiempoTotalBloqueo += procesoEnEjecucion.getTiempoBloqueado();
                    // hay cambio de contexto porque sale de CPU
                    aplicarCambioContexto(procesoEnEjecucion);
                    procesoEnEjecucion = null;
                } 
                else if (procesoEnEjecucion.getCantidadInstrucciones() <= 0) {
                    // TERMINÓ
                    if (colaDelProceso != null) {
                        colaDelProceso.terminarProceso(procesoEnEjecucion); // esto también remueve de procesosActuales
                    }
                    aplicarCambioContexto(procesoEnEjecucion); // CDC al quitar del CPU
                    procesoEnEjecucion = null;
                } 
                else if (tiempoRestanteQuantum <= 0) {
                    // Quantum agotado: volver a listo y reinsertar al final de su cola
                    procesoEnEjecucion.listo();
                    procesoEnEjecucion.incrementarTiempoCambioContexto();
                    if (colaDelProceso != null) {
                        colaDelProceso.reinsertarProceso(procesoEnEjecucion);
                    }
                    aplicarCambioContexto(procesoEnEjecucion);
                    procesoEnEjecucion = null;
                }
            }

            // Actualizar tiempos de espera (todos los procesos listos que no estén en CPU)
            actualizarTiemposEspera();

            // Añadir snapshot de estado
            infoActual.add(mostrarTablaEstado());

            // avanzar tiempo global (si no avanzamos ya por CDC; CDC ya suma tiempoActual)
            tiempoActual++;
        }
        return infoActual;
    }

    private void actualizarTiemposEspera() {
        for (ColaProcesos cola : colas) {
            for (Proceso p : cola.getProcesosActuales()) {
                if (p.getEstado() == EstadoProceso.Listo && p != procesoEnEjecucion) {
                    p.incrementarTiempoEnCola();
                    tiempoTotalEspera++;
                }
                if (p.getEstado() == EstadoProceso.Bloqueado) {
                    // contabilizado en procesarBloqueados con disminución; ya contamos el totalBloqueo cuando se bloqueó
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

    private String mostrarTablaEstado() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("%-6s %-6s %-6s %-6s %-9s %-3s %-7s %-5s %-5s%n",
                "Proc", "IdCola", "T.enCol", "Inst.", "Estado", "CDC", "Bloqueo", "Exe", "Total"));
        sb.append("-------------------------------------------------------------------\n");

        for (ColaProcesos cola : colas) {
            sb.append(String.format("--- Cola %d ---\n", cola.getIdCola()));
            for (Proceso proceso : cola.getProcesosActuales()) {
                sb.append(String.format("P%-5d %-6d %-6d %-6d %-9s %-3d %-7d %-5d %-5d%n",
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
            sb.append("-------------------------------------------------------------------\n");
        }

        sb.append(String.format("Proceso en ejecucion: %s%n",
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

    public void mostrarMetricas() {
        System.out.println("=== MÉTRICAS FINALES ===");
        System.out.println("Tiempo total de ejecución: " + tiempoTotalEjecucion);
        System.out.println("Tiempo total de cambio de contexto: " + tiempoTotalCambioContexto);
        System.out.println("Tiempo total de bloqueo: " + tiempoTotalBloqueo);
        System.out.println("Tiempo total de espera en cola: " + tiempoTotalEspera);
        System.out.println("Tiempo total de simulación: " + tiempoActual);

        for (int i = 0; i < colas.size(); i++) {
            ColaProcesos cola = colas.get(i);
            System.out.println("Cola " + (i + 1) + " - Procesos terminados: " +
                    cola.getProcesosTerminados().size());
        }
    }

    public ColaProcesos getCola(int indice) {
        if (indice >= 0 && indice < colas.size()) return colas.get(indice);
        return null;
    }

    public List<ColaProcesos> getColas() { return colas; }
}
