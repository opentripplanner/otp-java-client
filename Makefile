release:
	git checkout main
	git pull
	mvn release:clean release:prepare release:perform -Dgoals=deploy release:clean

SCHEMA:="types/src/main/resources/schema.graphqls"

update-schema:
	curl "https://raw.githubusercontent.com/opentripplanner/OpenTripPlanner/refs/heads/dev-2.x/application/src/main/resources/org/opentripplanner/apis/gtfs/schema.graphqls" -o ${SCHEMA}
	sed -i 's/QueryType/Query/g' ${SCHEMA}
	sed -i '/Needed until https:\/\/github.com\/facebook\/relay\/issues\/112 is resolved/d' ${SCHEMA}
	sed -i '/viewer: Query/d' ${SCHEMA}

