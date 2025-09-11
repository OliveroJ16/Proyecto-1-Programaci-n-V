public enum EstadoProceso {
    Nuevo,
    Listo,
    Ejecucion,
    Bloqueado,
    Terminado
}

/*
 * public enum EstadoProceso {
    Nuevo,
    Listo,
    Ejecucion,
    Bloqueado,
    Terminado;

    @Override
    public String toString() {
        switch (this) {
            case Nuevo: return "N";
            case Listo: return "L";
            case Ejecucion: return "E";
            case Bloqueado: return "B";
            case Terminado: return "T";
            default: return super.toString();
        }
    }
}

 */
