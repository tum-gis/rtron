package io.rtron.transformer.modifiers.opendrive.reprojector

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

class OpendriveReprojectorTest :
    FunSpec({

        test("basic coordinate projection works") {
            val crsFactory = CRSFactory()
            val sourceCrs: CoordinateReferenceSystem =
                crsFactory.createFromParameters(
                    "WGS84",
                    "+proj=tmerc +lat_0=48.1485460905528 +lon_0=11.5679503890009 +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs",
                )
            val targetCrs = crsFactory.createFromName("epsg:25832")
            val wgsToUtm: CoordinateTransform = CoordinateTransformFactory().createTransform(sourceCrs, targetCrs)
            val sourceCoordinate = ProjCoordinate(167.3432219777634, -277.563889501929, 0.15239999999999998)

            val result = ProjCoordinate()
            wgsToUtm.transform(sourceCoordinate, result)

            result.x.shouldBe(691176.5699790819 plusOrMinus 0.000001)
            result.y shouldBe (5335728.14692931 plusOrMinus 0.000001)
        }
    })
