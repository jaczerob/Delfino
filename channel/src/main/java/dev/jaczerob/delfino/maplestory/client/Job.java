package dev.jaczerob.delfino.maplestory.client;

public enum Job {
    BEGINNER(0),

    GM(900),
    SUPER_GM(910);

    final static int maxId = 22;    // maxId = (EVAN / 100);
    final int jobid;

    Job(int id) {
        jobid = id;
    }

    public static int getMax() {
        return maxId;
    }

    public static Job getById(int id) {
        for (Job l : Job.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public int getId() {
        return jobid;
    }

    public boolean isA(Job basejob) {
        int basebranch = basejob.getId() / 10;
        return (getId() / 10 == basebranch && getId() >= basejob.getId()) || (basebranch % 10 == 0 && getId() / 100 == basejob.getId() / 100);
    }

    public int getJobNiche() {
        return (jobid / 100) % 10;
    }
}
