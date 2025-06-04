release:
	git checkout main
	git pull
	mvn release:clean release:prepare release:perform -Dgoals=deploy release:clean

update-schema:
	curl "https://raw.githubusercontent.com/opentripplanner/OpenTripPlanner/refs/heads/dev-2.x/application/src/main/resources/org/opentripplanner/apis/gtfs/schema.graphqls" -o types/src/main/resources/schema.graphqls
