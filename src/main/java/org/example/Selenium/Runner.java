package org.example.Selenium;

public class Runner {

    private int bib;
    private String runnerName;
    private String country;
    private String finisherTime;

    public Runner(int bib, String runnerName, String country, String finisherTime) {
        this.bib = bib;
        this.runnerName = runnerName;
        this.country = country;
        this.finisherTime = finisherTime;
    }

    public int getBib() {
        return bib;
    }

    public void setBib(int bib) {
        this.bib = bib;
    }

    public String getRunnerName() {
        return runnerName;
    }

    public void setRunnerName(String runnerName) {
        this.runnerName = runnerName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFinisherTime() {
        return finisherTime;
    }

    public void setFinisherTime(String finisherTime) {
        this.finisherTime = finisherTime;
    }

    @Override
    public String toString() {
        return "Runner10kmNonComp{" +
                "bib=" + bib +
                ", runnerName='" + runnerName + '\'' +
                ", country='" + country + '\'' +
                ", finisherTime='" + finisherTime + '\'' +
                '}';
    }
}
