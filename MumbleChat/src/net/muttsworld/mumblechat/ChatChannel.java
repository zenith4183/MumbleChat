package net.muttsworld.mumblechat;

public class ChatChannel {

    private String name;
    private String permission;
    private Boolean muteable;
    private String color;
    private Boolean defaultChannel;
    private String alias;
    private Double distance;
    private Boolean filter;

    ChatChannel(String _Name, String _color, String _Permission,
            Boolean _muteable, Boolean _filter, Boolean _defaultchannel,
            String _alias, Double _distance) {
        name = _Name;
        permission = _Permission;
        muteable = _muteable;
        setColor(_color);
        setDefaultChannel(_defaultchannel);
        setAlias(_alias);
        setDistance(_distance);
        setFilter(_filter);
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public Boolean isMuteable() {
        return muteable;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean isDefaultchannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(Boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Boolean isDistance() {
        if (distance > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean hasPermission() {
        if (permission.equalsIgnoreCase("None")) {
            return false;
        }
        return true;
    }

    public Boolean isFiltered() {
        return filter;
    }

    public void setFilter(Boolean filter) {
        this.filter = filter;
    }
}
