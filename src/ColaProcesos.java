import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ColaProcesos {
    private final int idCola;
    private Queue<Proceso> procesosListos;
    private List<Proceso> procesosBloqueados;
    private List<Proceso> procesosTerminados;
    private List<Proceso> procesosActuales;
    
    public ColaProcesos(int idCola) {
        this.idCola = idCola;
        this.procesosListos= new LinkedList<>();
        this.procesosBloqueados = new ArrayList<>();
        this.procesosTerminados = new ArrayList<>();
        this.procesosActuales = new ArrayList<>();
    }

    public void agregarProceso(Proceso proceso){
        procesosListos.offer(proceso);
        procesosActuales.add(proceso);
    }

    public void reinsertarProceso(Proceso proceso) {
        procesosListos.offer(proceso);
    }

    public Proceso obtenerProceso(){
        return procesosListos.poll();
    }

    public boolean procesosListosVacia(){
        return procesosListos.isEmpty();
    }

    // Bloquea el proceso (el tiempo de bloqueo debe haberse asignado previamente vía asignarTiempoBloqueado)
    public void bloquearProceso(Proceso proceso) {
        proceso.bloquear();
        procesosBloqueados.add(proceso);
        // No lo removemos de procesosActuales aquí, porque lo seguimos mostrando hasta que termine el bloqueo y salga de la cola actual
    }

    // TERMINAR: mover a terminados y eliminar de la lista general de procesos actuales
    public void terminarProceso(Proceso proceso){
        proceso.terminar();
        procesosTerminados.add(proceso);
        procesosActuales.remove(proceso); // <-- importante: ya no se muestra en estado final
    }
    
    public void procesarBloqueados() {
        Iterator<Proceso> iterator = procesosBloqueados.iterator();
        while (iterator.hasNext()) {
            Proceso proceso = iterator.next();
            proceso.disminuirTiempoBloqueo();
            
            if (proceso.getTiempoBloqueado() <= 0) {
                proceso.listo();
                procesosListos.offer(proceso);
                iterator.remove();
            }
        }
    }

    public int getIdCola(){ 
        return idCola;
    }

    public Queue<Proceso> getColaListos() { 
        return procesosListos; 
    }

    public List<Proceso> getProcesosBloqueados() { 
        return procesosBloqueados; 
    }

    public List<Proceso> getProcesosTerminados() { 
        return procesosTerminados; 
    }

    public List<Proceso> getProcesosActuales() { 
        return procesosActuales; 
    }
}
