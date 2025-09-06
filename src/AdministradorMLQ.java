import java.util.LinkedList;
import java.util.Queue;

public class AdministradorMLQ {
    
    private int cambioContexto;
    private Queue<Cola> colas;
    private int quantum;
    private int procesosPorCola;

    //Contructor
    public AdministradorMLQ(int cambioContexto, int quantum, int procesosPorCola) {
        this.cambioContexto = cambioContexto;
        this.quantum = quantum;
        this.procesosPorCola = procesosPorCola;
        this.colas = new LinkedList<>();
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

