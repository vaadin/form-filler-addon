package org.vaadin.addons.ai.formfiller.utils;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.Gson;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// https://github.com/sshniro/line-segmentation-algorithm-to-gcp-vision/blob/master/kotlin/src/main/kotlin/com/google/vision/linesegmentation/GoogleVisionLineSegmentationParser.kt
// https://stackoverflow.com/questions/42391009/text-extraction-line-by-line

public class GoogleVisionLineSegmentationParser {

    @SuppressWarnings("unchecked")
    public List<String> initLineSegmentation(AnnotateImageResponse data) {
        int yMax = getYMax(data.getTextAnnotations(0));

        AnnotateImageResponse newData = invertAxis(data, yMax);
        List<String> lines = new ArrayList<>(Arrays.asList(newData.getTextAnnotationsList().get(0).getDescription().split("\n")));
        Collections.reverse(lines);

        List<EntityAnnotation> rawText = new ArrayList<>(newData.getTextAnnotationsList());
        Collections.reverse(rawText);
        rawText.remove(rawText.size() - 1);

        List<EntityAnnotation> mergedArray = getMergedLines(lines, rawText);
        List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata = getBoundingPolygon(mergedArray);

        combineBoundingPolygon(entityToMetadata);
        return constructLineWithBoundingPolygon(entityToMetadata);
    }

    protected List<String> constructLineWithBoundingPolygon(List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata) {
        ArrayList<String> finalArray = new ArrayList<>();
        for (int index = 0; index < entityToMetadata.size(); index++) {
            Pair<EntityAnnotation, EntityMetadata> it = entityToMetadata.get(index);
            if (!it.getSecond().matched) {
                if (it.getSecond().match.isEmpty()) {
                    finalArray.add(it.getFirst().getDescription());
                } else {
                    finalArray.add(arrangeWordsInOrder(entityToMetadata, index));
                }
            }
        }
        return finalArray;
    }

    private List<EntityAnnotation> getMergedLines(List<String> lines, List<EntityAnnotation> rawText) {
        ArrayList<EntityAnnotation> mergedArray = new ArrayList<>();
        while (lines.size() != 1) {
            String l = lines.remove(lines.size() - 1);
            String l1 = l;
            boolean status = true;
            EntityAnnotation mergedElement = null;

            while (true) {
                if (rawText.isEmpty()) {
                    break;
                }
                EntityAnnotation wElement = rawText.remove(rawText.size() - 1);
                String w = wElement.getDescription();

                int index = l.indexOf(w);

                l = l.substring(index + w.length());

                if (status) {
                    status = false;
                    mergedElement = wElement;
                }
                if (l.equals("")) {
                    EntityAnnotation newElement = EntityAnnotation.newBuilder().mergeFrom(mergedElement)
                            .setDescription(l1)
                            .setBoundingPoly(BoundingPoly.newBuilder().mergeFrom(mergedElement.getBoundingPoly()).clearVertices()
                                    .addVertices(0, mergedElement.getBoundingPoly().getVerticesList().get(0))
                                    .addVertices(1, wElement.getBoundingPoly().getVerticesList().get(1))
                                    .addVertices(2, wElement.getBoundingPoly().getVerticesList().get(2))
                                    .addVertices(3, mergedElement.getBoundingPoly().getVerticesList().get(3)).build()).build();
                    mergedArray.add(newElement);
                    break;
                }
            }
        }
        return mergedArray;
    }

    private String arrangeWordsInOrder(List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata, int k) {
        String mergedLine = "";
        List<Match> line = entityToMetadata.get(k).getSecond().getMatch();

        for (Match match : line) {
            int index = match.getMatchLineNum();
            String matchedWordForLine = entityToMetadata.get(index).getFirst().getDescription();

            int mainX = entityToMetadata.get(k).getFirst().getBoundingPoly().getVerticesList().get(0).getX();
            int compareX = entityToMetadata.get(index).getFirst().getBoundingPoly().getVerticesList().get(0).getX();

            if (compareX > mainX) {
                mergedLine = entityToMetadata.get(k).getFirst().getDescription() + ' ' + matchedWordForLine;
            } else {
                mergedLine = matchedWordForLine + ' ' + entityToMetadata.get(k).getFirst().getDescription();
            }
        }
        return mergedLine;
    }

    public int getYMax(EntityAnnotation data) {
        int maxY = Integer.MIN_VALUE;
        for (Vertex vertex : data.getBoundingPoly().getVerticesList()) {
            if (vertex.getY() > maxY) {
                maxY = vertex.getY();
            }
        }
        return maxY;
    }

    private AnnotateImageResponse invertAxis(AnnotateImageResponse data, int yMax) {
        List<EntityAnnotation> newEntities = new ArrayList<>();
        newEntities.add(data.getTextAnnotationsList().get(0));
        for (int i = 1; i < data.getTextAnnotationsList().size(); i++) {
            List<Vertex> vertexList = new ArrayList<>();
            for (Vertex vertex : data.getTextAnnotationsList().get(i).getBoundingPoly().getVerticesList()) {
                vertexList.add(Vertex.newBuilder().mergeFrom(vertex).clearY().setY(yMax - vertex.getY()).build());
            }
            EntityAnnotation.Builder entityBuilder = EntityAnnotation.newBuilder().mergeFrom(data.getTextAnnotationsList().get(i));
            entityBuilder.setBoundingPoly(entityBuilder.getBoundingPolyBuilder().clearVertices().addAllVertices(vertexList).build());
            EntityAnnotation newEntity = entityBuilder.build();
            newEntities.add(newEntity);
        }
        AnnotateImageResponse.Builder responseBuilder = AnnotateImageResponse.newBuilder().mergeFrom(data);
        responseBuilder.clearTextAnnotations();
        responseBuilder.addAllTextAnnotations(newEntities);
        return responseBuilder.build();
    }

    private List<Pair<EntityAnnotation, EntityMetadata>> getBoundingPolygon(List<EntityAnnotation> mergedArray) {
        List<Pair<EntityAnnotation, EntityMetadata>> entityAnnotationToMetadata = new ArrayList<>();
        for (int index = 0; index < mergedArray.size(); index++) {
            EntityAnnotation it = mergedArray.get(index);
            List<Vertex> arr = new ArrayList<>();
            int h1 = it.getBoundingPoly().getVerticesList().get(0).getY() - it.getBoundingPoly().getVerticesList().get(3).getY();
            int h2 = it.getBoundingPoly().getVerticesList().get(1).getY() - it.getBoundingPoly().getVerticesList().get(2).getY();
            int h = h1;
            if (h2 > h1) {
                h = h2;
            }
            int avgHeight = (int) (h * 0.6);

            arr.add(it.getBoundingPoly().getVerticesList().get(1));
            arr.add(it.getBoundingPoly().getVerticesList().get(0));
            Rectangle line1 = getRectangle(arr, avgHeight, true);

            arr = new ArrayList<>();
            arr.add(it.getBoundingPoly().getVerticesList().get(2));
            arr.add(it.getBoundingPoly().getVerticesList().get(3));
            Rectangle line2 = getRectangle(arr, avgHeight, false);

            entityAnnotationToMetadata.add(new Pair<>(it, new EntityMetadata(createPolygon(line1, line2), index, new ArrayList<>(), false)));
        }
        return entityAnnotationToMetadata;
    }

    private void combineBoundingPolygon(List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata) {
        // select one word from the array
        for (int index1 = 0; index1 < entityToMetadata.size(); index1++) {
            Pair<EntityAnnotation, EntityMetadata> it = entityToMetadata.get(index1);
            Polygon bigBB = it.getSecond().getBigBB();
            // iterate through all the array to find the match
            for (int index2 = index1; index2 < entityToMetadata.size(); index2++) {
                Pair<EntityAnnotation, EntityMetadata> k = entityToMetadata.get(index2);
                // Do not compare with the own bounding box and which was not matched with a line
                if (index1 != index2 && !k.getSecond().isMatched()) {
                    int insideCount = 0;
                    for (Vertex coordinate : k.getFirst().getBoundingPoly().getVerticesList()) {
                        if (bigBB.contains(coordinate.getX(), coordinate.getY())) {
                            insideCount += 1;
                        }
                    }
                    // all four points were inside the big bb
                    if (insideCount == 4) {
                        it.getSecond().getMatch().add(new Match(insideCount, index2));
                        k.getSecond().setMatched(true);
                    }
                }
            }
        }
    }


    private Rectangle getRectangle(List<Vertex> v, double avgHeight, boolean isAdd) {
        double firstCandidate;
        double secondCandidate;
        if (isAdd) {
            secondCandidate = v.get(1).getY() + avgHeight;
            firstCandidate = v.get(0).getY() + avgHeight;
        } else {
            secondCandidate = v.get(1).getY() - avgHeight;
            firstCandidate = v.get(0).getY() - avgHeight;
        }

        double yDiff = secondCandidate - firstCandidate;
        double xDiff = v.get(1).getX() - v.get(0).getX();

        double gradient = yDiff / xDiff;

        int xThreshMin = 1;
        int xThreshMax = 2000;

        double yMin;
        double yMax;
        if (gradient == 0.0) {
            yMin = firstCandidate;
            yMax = firstCandidate;
        } else {
            yMin = (firstCandidate) - (gradient * (v.get(0).getX() - xThreshMin));
            yMax = (firstCandidate) + (gradient * (xThreshMax - v.get(0).getX()));
        }
        yMin = Math.round(yMin);
        yMax = Math.round(yMax);
        return new Rectangle(xThreshMin, xThreshMax, yMin, yMax);
    }

    private Polygon createPolygon(Rectangle line1, Rectangle line2) {
        Polygon polygon = new Polygon();
        polygon.addPoint(line1.getxMin(), (int) Math.round(line1.getyMin()));
        polygon.addPoint(line1.getxMax(), (int) Math.round(line1.getyMax()));
        polygon.addPoint(line2.getxMax(), (int) Math.round(line2.getyMax()));
        polygon.addPoint(line2.getxMin(), (int) Math.round(line2.getyMin()));
        return polygon;
    }

    private Object deepCopy(Object t) {
        String serializedObj = new Gson().toJson(t);
        return new Gson().fromJson(serializedObj, AnnotateImageResponse.class);
    }

    static class Rectangle {
        private int xMin;
        private int xMax;
        private double yMin;
        private double yMax;

        public Rectangle(int xMin, int xMax, double yMin, double yMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }

        public int getxMin() {
            return xMin;
        }

        public void setxMin(int xMin) {
            this.xMin = xMin;
        }

        public int getxMax() {
            return xMax;
        }

        public void setxMax(int xMax) {
            this.xMax = xMax;
        }

        public double getyMin() {
            return yMin;
        }

        public void setyMin(double yMin) {
            this.yMin = yMin;
        }

        public double getyMax() {
            return yMax;
        }

        public void setyMax(double yMax) {
            this.yMax = yMax;
        }
    }

    static class EntityMetadata {
        private final Polygon bigBB;
        private int lineNum;
        private final List<Match> match;
        private boolean matched;

        public EntityMetadata(Polygon bigBB, int lineNum, List<Match> match, boolean matched) {
            this.bigBB = bigBB;
            this.lineNum = lineNum;
            this.match = match;
            this.matched = matched;
        }

        public Polygon getBigBB() {
            return bigBB;
        }

        public List<Match> getMatch() {
            return match;
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
        }

        public boolean isMatched() {
            return matched;
        }
    }

    static class Match {
        private final int matchCount;
        private final int matchLineNum;

        public Match(int matchCount, int matchLineNum) {
            this.matchCount = matchCount;
            this.matchLineNum = matchLineNum;
        }

        public int getMatchCount() {
            return matchCount;
        }

        public int getMatchLineNum() {
            return matchLineNum;
        }
    }
}
