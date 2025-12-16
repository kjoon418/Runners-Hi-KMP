package good.space.runnershi.mapper

import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.dto.running.LocationPoint
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.util.TimeConverter

object RunMapper {
    fun mapToCreateRequest(domain: RunResult): RunCreateRequest {
        val flatLocations = mutableListOf<LocationPoint>()
        var globalOrder = 0

        // List<List<Location>> -> Flat List<LocationDto> 변환
        domain.pathSegments.forEachIndexed { sIndex, segment ->
            segment.forEach { loc ->
                flatLocations.add(
                    LocationPoint(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        timestamp = TimeConverter.toIso8601(loc.timestamp), // Long -> ISO String 변환
                        segmentIndex = sIndex, // 세그먼트 인덱스 주입
                        sequenceOrder = globalOrder++
                    )
                )
            }
        }

        return RunCreateRequest(
            distanceMeters = domain.totalDistanceMeters,
            durationSeconds = domain.durationSeconds,
            startedAt = TimeConverter.toIso8601(domain.startedAt), // Long -> ISO String
            locations = flatLocations
        )
    }
}

