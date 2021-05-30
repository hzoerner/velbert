package org.matsim.velbert.analysis;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ShapeAnalysis {

    private static final String shapeFile = "C:\\Users\\anton\\IdeaProjects\\Hausaufgaben\\HA1\\openstreetmap\\OSM_PLZ_072019.shp";
    private static final String populationPath = "C:\\Users\\anton\\IdeaProjects\\Hausaufgaben\\HA1\\output\\plans.xml.gz";
    private static final String networkPath = "C:\\Users\\anton\\IdeaProjects\\Hausaufgaben\\HA1\\output\\network.xml.gz";
    private static final CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:25832", "EPSG:3857");
    //HashMap content: postal code (plz) as String, counter as Integer
    private static final HashMap<String, Integer> plzMap = new HashMap<String, Integer>();
    //Printer

    public static void main(String[] args) throws IOException {
        PrintWriter pWriter = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\anton\\IdeaProjects\\Hausaufgaben\\HA1\\output\\shapeanalysis.csv")));
        //Set postal codes (add more if needed!)
        plzMap.put("42553", 0);
        plzMap.put("42555", 0);
        plzMap.put("42551", 0);
        plzMap.put("42549", 0);

        //perform Analysis for each postal code set
        for (String key : plzMap.keySet()) {

            //most of this code was done in the Seminar
            var features = ShapeFileReader.getAllFeatures(shapeFile);
            var network = NetworkUtils.readNetwork(networkPath);
            var population = PopulationUtils.readPopulation(populationPath);

            var geometry = features.stream()
                    .filter(feature -> feature.getAttribute("plz").equals(key))
                    .map(feature -> (Geometry) feature.getDefaultGeometry())
                    .findAny()
                    .orElseThrow();

            int counter = 0;

            for (var person : population.getPersons().values()) {

                var plan = person.getSelectedPlan();
                var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

                for (var activity : activities) {
                    var coord = activity.getCoord();
                    var transformedCoord = transformation.transform(coord);

                    if (geometry.covers(MGC.coord2Point(transformedCoord))) {
                        counter++;
                    }
                }
            }
            //Write counter and postal code in output file
            pWriter.println(counter+";"+key);
        }
        //close writer
        pWriter.close();
    }
}
