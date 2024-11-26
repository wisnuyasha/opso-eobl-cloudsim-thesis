package org.cloudbus.cloudsim.examples;

import java.util.List;

import org.cloudbus.cloudsim.Vm;

public class Datacenter {
    private int id;
    private List<Vm> vmList;

    public Datacenter(int id, List<Vm> vmList) {
        this.id = id;
        this.vmList = vmList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Vm> getVmList() {
        return vmList;
    }

    public void setVmList(List<Vm> vmList) {
        this.vmList = vmList;
    }
}