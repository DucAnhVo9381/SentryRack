// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

public class DashboardModel {
    public final String rackName;
    public final String message;
    public final long timestamp;
    public final boolean critical;

    public DashboardModel(String rackName, String message, long timestamp, boolean critical) {
        this.rackName = rackName;
        this.message = message;
        this.timestamp = timestamp;
        this.critical = critical;
    }
}
