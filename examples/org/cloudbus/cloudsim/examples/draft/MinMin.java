package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class MinMin {
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    public MinMin(List<Cloudlet> cloudletList, List<Vm> vmList) {
        this.cloudletList = cloudletList;
        this.vmList = vmList;
    }

    /**
     * @param dataCenterIterator indeks datacenter yang sedang diproses (1-based)
     * @param cloudletIteration iterasi cloudlet
     * @return array of vmIds untuk cloudlet tersebut
     */
    public int[] scheduleTasks(int dataCenterIterator, int cloudletIteration) {
        // Index cloudlet yang akan dijadwalkan sama seperti di OPSO/PSO:
        int startIndex = 0 + (dataCenterIterator - 1) * 9 + cloudletIteration * 54;
        int endIndex = startIndex + 9; // 9 cloudlet untuk block ini
        int numberOfTasks = 9;

        // VM yang terkait datacenter ini:
        int minVmIndex = (dataCenterIterator - 1) * 9;
        int maxVmIndex = (dataCenterIterator * 9) - 1;
        int numberOfVms = 9;

        // Inisialisasi array untuk menyimpan penugasan VM untuk setiap cloudlet
        int[] bestSolution = new int[numberOfTasks];

        // Daftar tasks (cloudlet) yang belum dijadwalkan
        List<Integer> unscheduledTasks = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            unscheduledTasks.add(i);
        }

        // Load VM: waktu total yang sudah dijadwalkan pada VM tersebut
        double[] vmLoad = new double[numberOfVms];
        for (int i = 0; i < numberOfVms; i++) {
            vmLoad[i] = 0.0;
        }

        // Min-Min heuristic
        // Selama masih ada task yang belum dijadwalkan:
        while (!unscheduledTasks.isEmpty()) {
            double bestCompletionTime = Double.MAX_VALUE;
            int selectedTaskIndex = -1;
            int selectedVmIndex = -1;

            // Cari pasangan (task, vm) dengan completion time minimal
            for (int taskId : unscheduledTasks) {
                Cloudlet task = cloudletList.get(taskId);
                double taskLength = task.getCloudletLength();

                // Cek setiap VM yang tersedia untuk datacenter ini
                for (int vmOffset = 0; vmOffset < numberOfVms; vmOffset++) {
                    int vmId = minVmIndex + vmOffset;
                    Vm vm = vmList.get(vmId);
                    double mips = vm.getMips();

                    double execTime = taskLength / mips;
                    double completionTime = vmLoad[vmOffset] + execTime;

                    if (completionTime < bestCompletionTime) {
                        bestCompletionTime = completionTime;
                        selectedTaskIndex = taskId;
                        selectedVmIndex = vmOffset;
                    }
                }
            }

            // Jadwalkan task terpilih ke VM terpilih
            // Update vmLoad
            vmLoad[selectedVmIndex] = bestCompletionTime;
            // Masukkan penugasan ke bestSolution:
            // Indeks di bestSolution harus berdasarkan urutan task di block ini
            int solutionIndex = selectedTaskIndex - startIndex;
            bestSolution[solutionIndex] = minVmIndex + selectedVmIndex;

            // Hapus task ini dari daftar unscheduled
            unscheduledTasks.remove((Integer) selectedTaskIndex);
        }

        return bestSolution;
    }
}
