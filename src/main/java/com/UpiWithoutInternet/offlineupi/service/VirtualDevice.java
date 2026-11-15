package com.UpiWithoutInternet.offlineupi.service;

import com.UpiWithoutInternet.offlineupi.model.MeshPacket;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualDevice {

    private final String deviceId;
    private final Boolean hasInternet;
    private final Map<String, MeshPacket> heldPacket = new ConcurrentHashMap<>();

    VirtualDevice(String deviceId ,Boolean hasInternet){
        this.deviceId=deviceId;
        this.hasInternet=hasInternet;
    }
    public String getDeviceId() { return deviceId; }
    public boolean hasInternet() { return hasInternet; }

    public void hold(MeshPacket packet) {
        heldPacket.putIfAbsent(packet.getPacketId(), packet);
    }

    public Collection<MeshPacket> getHeldPackets() {
        return heldPacket.values();
    }

    public boolean holds(String packetId) {
        return heldPacket.containsKey(packetId);
    }

    public int packetCount() {
        return heldPacket.size();
    }

    public void clear() {
        heldPacket.clear();
    }
}
