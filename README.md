> gradle build
> java -javaagent:histo-dump-agent-1.0.jar -classpath . YourClass

That will generate histo dumps at 1 min interval into /tmp/histo-dump-<timestamp>/ directory

You can generate a report by running the tool
> java -jar histo-dump-agent-1.0.jar /tmp/histo-dump-<timestamp>/ /tmp/report/
