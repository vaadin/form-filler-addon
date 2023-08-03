package org.vaadin.addons.ai.formfiller.utils

import com.google.cloud.vision.v1.AnnotateImageResponse
import com.google.cloud.vision.v1.BoundingPoly
import com.google.cloud.vision.v1.EntityAnnotation
import com.google.cloud.vision.v1.Vertex
import com.google.gson.Gson
import java.awt.Polygon
import kotlin.math.round

// https://github.com/sshniro/line-segmentation-algorithm-to-gcp-vision/blob/master/kotlin/src/main/kotlin/com/google/vision/linesegmentation/GoogleVisionLineSegmentationParser.kt
// https://stackoverflow.com/questions/42391009/text-extraction-line-by-line

class GoogleVisionLineSegmentationParser {
    /**
     * GCP Vision groups several nearby words to appropriate lines
     * But will not group words that are too far away
     * This function combines nearby words and create a combined bounding polygon
     */
    @Suppress("UNCHECKED_CAST")
    fun initLineSegmentation(data: AnnotateImageResponse): List<String> {
        val yMax = getYMax(data.getTextAnnotations(0))

        val newData = invertAxis(data, yMax)
        // The first index refers to the auto identified words which belongs to a sings line
        var lines = newData.textAnnotationsList[0].description.split('\n').toMutableList()
        // gcp vision full text
        var rawText = (deepCopy(newData) as AnnotateImageResponse).textAnnotationsList

        // reverse to use lifo, because array.shift() will consume 0(n)
        lines = lines.reversed().toMutableList()
        rawText = rawText.reversed().toMutableList()
        //to remove the zeroth element which gives the total summary of the text
        rawText.removeAt(rawText.size - 1)

        val mergedArray = getMergedLines(lines, rawText);
        val entityToMetadata = getBoundingPolygon(mergedArray);

        combineBoundingPolygon(entityToMetadata);
        return constructLineWithBoundingPolygon(entityToMetadata);
    }

    // TODO implement the line ordering for multiple words
    protected fun constructLineWithBoundingPolygon(entityToMetadata: List<Pair<EntityAnnotation, EntityMetadata>>): List<String> {
        var finalArray = ArrayList<String>();
        entityToMetadata.forEachIndexed { index, it ->
            if (!it.second.matched) {
                if (it.second.match.size == 0) {
                    finalArray.add(it.first.description)
                } else {
                    // arrangeWordsInOrder(mergedArray, i);
                    // let index = mergedArray[i]['match'][0]['matchLineNum'];
                    // let secondPart = mergedArray[index].description;
                    // finalArray.push(mergedArray[i].description + ' ' +secondPart);
                    finalArray.add(arrangeWordsInOrder(entityToMetadata, index));
                }
            }
        }
        return finalArray;
    }

    private fun getMergedLines(lines: MutableList<String>, rawText: MutableList<EntityAnnotation>): List<EntityAnnotation> {
        val mergedArray = ArrayList<EntityAnnotation>();
        while (lines.size != 1) {
            var l = lines.removeAt(lines.size - 1);
            val l1 = l
            var status = true;
            var mergedElement: EntityAnnotation? = null

            while (true) {
                if (rawText.isEmpty()) {
                    break;
                }
                val wElement = rawText.removeAt(rawText.size - 1);
                val w = wElement.description;

                val index = l.indexOf(w);

                l = l.substring(index + w.length);

                if (status) {
                    status = false;
                    // set starting coordinates
                    mergedElement = wElement;
                }
                if (l == "") {
                    val newElement = EntityAnnotation.newBuilder().mergeFrom(mergedElement)
                            .setDescription(l1)
                            .setBoundingPoly(BoundingPoly.newBuilder().mergeFrom(mergedElement!!.boundingPoly).clearVertices()
                                    .addVertices(0, mergedElement.boundingPoly.verticesList[0])
                                    .addVertices(1, wElement.boundingPoly.verticesList[1])
                                    .addVertices(2, wElement.boundingPoly.verticesList[2])
                                    .addVertices(3, mergedElement.boundingPoly.verticesList[3]).build()).build()
                    mergedArray.add(newElement);
                    break;
                }
            }
        }
        return mergedArray;
    }

    private fun arrangeWordsInOrder(entityToMetadata: List<Pair<EntityAnnotation, EntityMetadata>>, k: Int): String {
        var mergedLine = "";
        var line = entityToMetadata[k].second.match;

        line.forEach {
            val index = it.matchLineNum;
            val matchedWordForLine = entityToMetadata[index].first.description;

            val mainX = entityToMetadata[k].first.boundingPoly.verticesList[0].x;
            val compareX = entityToMetadata[index].first.boundingPoly.verticesList[0].x;

            if (compareX > mainX) {
                mergedLine = entityToMetadata[k].first.description + ' ' + matchedWordForLine;
            } else {
                mergedLine = matchedWordForLine + ' ' + entityToMetadata[k].first.description;
            }
        }
        return mergedLine;
    }

    /**
     * @Method computes the maximum y coordinate from the identified text blob
     * @param data
     * @returns {*}
     */
    fun getYMax(data: EntityAnnotation): Int {
        var maxY = Int.MIN_VALUE
        for (vertex in data.boundingPoly.verticesList) {
            if (vertex.y > maxY) {
                maxY = vertex.y
            }
        }
        return maxY
    }

    /**
     * @Method inverts the y axis coordinates for easier computation
     * as the google vision starts the y axis from the bottom
     * @param data
     * @param yMax
     * @returns {*}
     */
    private fun invertAxis(data: AnnotateImageResponse, yMax: Int): AnnotateImageResponse {
        //TODO Don't think this is needed
        //data = fillMissingValues(data);
        val newEntities = ArrayList<EntityAnnotation>()
        newEntities.add(data.textAnnotationsList[0])
        for (i in 1 until data.textAnnotationsList.size) {
            val vertexList = ArrayList<Vertex>()
            data.textAnnotationsList[i].boundingPoly.verticesList.forEach {
                vertexList.add(Vertex.newBuilder().mergeFrom(it).clearY().setY(yMax - it.y).build())
            }
            val entityBuilder = EntityAnnotation.newBuilder().mergeFrom(data.textAnnotationsList[i])
            entityBuilder.boundingPoly = entityBuilder.boundingPolyBuilder.clearVertices().addAllVertices(vertexList).build()
            val newEntity = entityBuilder.build()
            newEntities.add(newEntity)
        }
        val responseBuilder = AnnotateImageResponse.newBuilder().mergeFrom(data)
        responseBuilder.clearTextAnnotations()
        responseBuilder.addAllTextAnnotations(newEntities)
        return responseBuilder.build()
    }


    /**
     *
     * @param mergedArray
     */
    private fun getBoundingPolygon(mergedArray: List<EntityAnnotation>): List<Pair<EntityAnnotation, EntityMetadata>> {
        val entityAnnotationToMetadata = ArrayList<Pair<EntityAnnotation, EntityMetadata>>()
        mergedArray.forEachIndexed { index, it ->
            var arr = ArrayList<Vertex>();
            // calculate line height
            val h1 = it.boundingPoly.verticesList[0].y - it.boundingPoly.verticesList[3].y;
            val h2 = it.boundingPoly.verticesList[1].y - it.boundingPoly.verticesList[2].y;
            var h = h1;
            if (h2 > h1) {
                h = h2
            }
            val avgHeight = h * 0.6;

            arr.add(it.boundingPoly.verticesList[1]);
            arr.add(it.boundingPoly.verticesList[0]);
            val line1 = getRectangle(arr, avgHeight, true);

            arr = ArrayList();
            arr.add(it.boundingPoly.verticesList[2]);
            arr.add(it.boundingPoly.verticesList[3]);
            val line2 = getRectangle(arr, avgHeight, false);

            entityAnnotationToMetadata.add(Pair(it, EntityMetadata(createPolygon(line1, line2), index, ArrayList(), false)))
        }
        return entityAnnotationToMetadata
    }

    private fun combineBoundingPolygon(entityToMetadata: List<Pair<EntityAnnotation, EntityMetadata>>) {
        // select one word from the array
        entityToMetadata.forEachIndexed { index1, it ->
            val bigBB = it.second.bigBB;
            // iterate through all the array to find the match
            for (index2 in index1 until entityToMetadata.size) {
                val k = entityToMetadata[index2]
                // Do not compare with the own bounding box and which was not matched with a line
                if (index1 != index2 && !k.second.matched) {
                    var insideCount = 0;
                    k.first.boundingPoly.verticesList.forEach { coordinate ->
                        if (bigBB.contains(coordinate.x, coordinate.y)) {
                            insideCount += 1;
                        }
                    }
                    // all four point were inside the big bb
                    if (insideCount == 4) {
                        it.second.match.add(Match(insideCount, index2))
                        k.second.matched = true;
                    }

                }
            }
        }
    }

    private fun getRectangle(v: MutableList<Vertex>, avgHeight: Double, isAdd: Boolean): Rectangle {
        val firstCandidate: Double
        val secondCandidate: Double
        if (isAdd) {
            secondCandidate = v[1].y + avgHeight
            firstCandidate = v[0].y + avgHeight
        } else {
            secondCandidate = v[1].y - avgHeight
            firstCandidate = v[0].y - avgHeight
        }

        val yDiff = (secondCandidate - firstCandidate);
        val xDiff = (v[1].x - v[0].x);

        val gradient = yDiff / xDiff;

        val xThreshMin = 1;
        val xThreshMax = 2000;

        var yMin: Double
        var yMax: Double
        if (gradient == 0.0) {
            // extend the line
            yMin = firstCandidate;
            yMax = firstCandidate;
        } else {
            yMin = (firstCandidate) - (gradient * (v[0].x - xThreshMin));
            yMax = (firstCandidate) + (gradient * (xThreshMax - v[0].x));
        }
        yMin = round(yMin);
        yMax = round(yMax);
        return Rectangle(xThreshMin, xThreshMax, yMin, yMax)
    }

    private fun createPolygon(line1: Rectangle, line2: Rectangle): Polygon {
        val polygon = Polygon()
        polygon.addPoint(line1.xMin, round(line1.yMin).toInt())
        polygon.addPoint(line1.xMax, round(line1.yMax).toInt())
        polygon.addPoint(line2.xMax, round(line2.yMax).toInt())
        polygon.addPoint(line2.xMin, round(line2.yMin).toInt())
        return polygon
    }

    private fun deepCopy(t: Any): Any {
        val serializedObj = Gson().toJson(t);
        return Gson().fromJson(serializedObj, AnnotateImageResponse::class.java)
    }

    data class Rectangle(
            val xMin: Int,
            val xMax: Int,
            val yMin: Double,
            val yMax: Double
    )

    data class EntityMetadata(
            val bigBB: Polygon,
            val lineNum: Int,
            val match: MutableList<Match>,
            var matched: Boolean
    )

    data class Match(
            val matchCount: Int,
            val matchLineNum: Int
    )
}