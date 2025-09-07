import java.util.Random;

public class Proceso {
    private int id;                    // Identificador del proceso
    private int idCola;
    private int numeroInstrucciones;   // Instrucciones restantes
    private EstadoProceso estado;      // LISTO, EJECUCION, BLOQUEADO, TERMINADO
    private int tiempoBloqueado;       // Tiempo que estará bloqueado (si aplica)
    private boolean requiereBloqueo;
    
    private int tiempoEnCola;          // Tiempo esperando turno en la cola
    private int tiempoCambioContexto;  // CDC aplicado a este proceso
    private int tiempoEjecucion;       // Tiempo que ha estado en CPU
    
    public Proceso() {
    }

    public Proceso(int id, int idCola, int numeroInstrucciones, EstadoProceso estado, int tiempoBloqueado,int tiempoEnCola, int tiempoCambioContexto, int tiempoEjecucion) {
        this.id = id;
        this.idCola = idCola;
        this.numeroInstrucciones = numeroInstrucciones;
        this.estado = estado;
        this.tiempoBloqueado = tiempoBloqueado;
        this.tiempoEnCola = tiempoEnCola;
        this.tiempoCambioContexto = tiempoCambioContexto;
        this.tiempoEjecucion = tiempoEjecucion;
    }

    public void simularBloqueado(){
        Random random = new Random();
        int duracion = random.nextInt(7) + 2; // Tiempo bloqueado 2 - 8
        this.estado = EstadoProceso.Bloqueado;
        this.tiempoBloqueado = duracion;;
    }


    public void generarNumeroInstrucciones(){
        Random random = new Random();
        int intrucciones = random.nextInt(96) + 5; // N intrucciones entre 5 - 100
        this.numeroInstrucciones = intrucciones;
    }

    public void aumentarTiempoEnCola(){
        this.tiempoEnCola ++;
    }

    public void disminuirTiempoInstrucciones(){
        this.numeroInstrucciones --;
    }

    public void aumentarTiempoEjecucion(){
        this.tiempoEjecucion ++;
    }

    @Override
    public String toString() {
        return "Proceso{" +
            "id=" + id +
            ", idCola=" + idCola +
            ", numeroInstrucciones=" + numeroInstrucciones +
            ", estado=" + estado +
            ", tiempoBloqueado=" + tiempoBloqueado +
            ", tiempoEnCola=" + tiempoEnCola +
            ", tiempoCambioContexto=" + tiempoCambioContexto +
            ", tiempoEjecucion=" + tiempoEjecucion +
            '}';
    }


    //Getter y Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCola() {
        return idCola;
    }

    public void setIdCola(int idCola) {
        this.idCola = idCola;
    }

    public int getNumeroInstrucciones() {
        return numeroInstrucciones;
    }

    public void setNumeroInstrucciones(int numeroInstrucciones) {
        this.numeroInstrucciones = numeroInstrucciones;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public int getTiempoBloqueado() {
        return tiempoBloqueado;
    }

    public void setTiempoBloqueado(int tiempoBloqueado) {
        this.tiempoBloqueado = tiempoBloqueado;
    }

    public int getTiempoEnCola() {
        return tiempoEnCola;
    }

    public void setTiempoEnCola(int tiempoEnCola) {
        this.tiempoEnCola = tiempoEnCola;
    }

    public int getTiempoCambioContexto() {
        return tiempoCambioContexto;
    }

    public void setTiempoCambioContexto(int tiempoCambioContexto) {
        this.tiempoCambioContexto = tiempoCambioContexto;
    }

    public int getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public boolean getRequireBloqueo(){
        return this.requiereBloqueo;
    }

    public void setRequireBloqueo(boolean requiereBloqueo){
        this.requiereBloqueo = requiereBloqueo;
    }

    public void setTiempoEjecucion(int tiempoEjecucion) {
        this.tiempoEjecucion = tiempoEjecucion;
    }

    

}
