public class Proceso {

    private final int idProceso;
    private final int idCola;
    private int cantidadInstrucciones;
    private EstadoProceso estado;
    private int tiempoCambioContexto;
    private boolean requiereBloqueo;
    private int tiempoBloqueado;
    private int tiempoEnCola;
    private int tiempoEjecucion;
    
    public Proceso(int idProceso, int idCola, int cantidadInstrucciones, boolean requiereBloqueo) {
        this.idProceso = idProceso;
        this.idCola = idCola;
        this.cantidadInstrucciones = cantidadInstrucciones;
        this.requiereBloqueo = requiereBloqueo;
        this.estado = EstadoProceso.Listo;
        this.tiempoCambioContexto = 0;
        this.tiempoEnCola = 0;
        this.tiempoEjecucion = 0;
    }

    public void listo(){
        this.estado = EstadoProceso.Listo;
    }

    public void bloquear() {
        this.estado = EstadoProceso.Bloqueado;
    }

    public void ejecutar() {
        this.estado = EstadoProceso.Ejecucion;
    }

    public void terminar() {
        this.estado = EstadoProceso.Terminado;
    }

    public int getTiempoTotal() {
        return tiempoEjecucion + tiempoEnCola + tiempoBloqueado + tiempoCambioContexto;
    }

    public void incrementarTiempoEjecucion() {
        this.tiempoEjecucion ++;
    }

    public void incrementarTiempoEnCola() {
        this.tiempoEnCola ++;
    }

    public void incrementarTiempoCambioContexto() {
        this.tiempoCambioContexto ++;
    }

    public void disminuirTiempoBloqueo() {
        this.tiempoBloqueado --;
    }

    public void ejecutarInstruccion() {
        if (estado == EstadoProceso.Ejecucion && cantidadInstrucciones > 0) {
            cantidadInstrucciones--;
        }
    }

    // Getters
    public int getIdProceso() { 
        return idProceso; 
    }

    public int getIdCola() { 
        return idCola; 
    }

    public int getCantidadInstrucciones() { 
        return cantidadInstrucciones; 
    }

    public EstadoProceso getEstado() { 
        return estado; 
    }

    public int getTiempoBloqueado() { 
        return tiempoBloqueado; 
    }

    public void asignarTiempoBloqueado(int tiempo){ 
        this.tiempoBloqueado = tiempo;
    }

    public int getTiempoEnCola() { 
        return tiempoEnCola; 
    }
    
    public int getTiempoCambioContexto() { 
        return tiempoCambioContexto; 
    }

    public int getTiempoEjecucion() { 
        return tiempoEjecucion; 
    }

    public boolean isRequiereBloqueo() { 
        return requiereBloqueo; 
    }

    public void setRequerirBloqueo(boolean bloqueo){ 
        requiereBloqueo = bloqueo; 
    }

}
