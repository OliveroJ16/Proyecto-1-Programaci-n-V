import java.util.LinkedList;
import java.util.Queue;

public class Cola {
    private int id;
    private Queue<Proceso> procesos;
    
    public Cola(int id) {
        this.id = id;
        this.procesos = new LinkedList<>();
    }

    //Metodo para crear la cola de procesos
    public Queue<Proceso> crearProcesos(int numeroProcesos){
        for (int i = 0; i < numeroProcesos; i++) {
            Proceso proceso = new Proceso(); // Verificar la instancia de este objeto (Usa un constructor vacio)
            proceso.setId(i);
            proceso.setIdCola(this.id);
            proceso.setEstado(EstadoProceso.Listo); //Todos los procesos empizan en "Listo"
            proceso.generarNumeroInstrucciones();
            this.procesos.add(proceso);
        }
        return this.procesos;
    }

    public Proceso sacarProceso() {
        return procesos.poll();
    }

    public boolean estaVacia() {
        return procesos.isEmpty();
    }


    //Getter y Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Queue<Proceso> getProcesos() {
        return procesos;
    }

    public void setProcesos(Queue<Proceso> procesos) {
        this.procesos = procesos;
    }
}

