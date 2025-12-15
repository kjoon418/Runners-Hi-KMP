package good.space.runnershi.mapper

import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.dto.LocationPointDto
import good.space.runnershi.model.dto.RunCreateRequestDto
import good.space.runnershi.util.TimeConverter

object RunMapper {
    fun mapToCreateRequest(domain: RunResult): RunCreateRequestDto {
        val flatLocations = mutableListOf<LocationPointDto>()
        var globalOrder = 0

        // List<List<Location>> -> Flat List<LocationDto> 변환
        domain.pathSegments.forEachIndexed { sIndex, segment ->
            segment.forEach { loc ->
                flatLocations.add(
                    LocationPointDto(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        timestamp = TimeConverter.toIso8601(loc.timestamp), // Long -> ISO String 변환
                        segmentIndex = sIndex, // 세그먼트 인덱스 주입
                        sequenceOrder = globalOrder++
                    )
                )
            }
        }

        return RunCreateRequestDto(
            title = "Morning Run", // 추후 사용자 입력 받거나 로직 추가 가능
            distanceMeters = domain.totalDistanceMeters,
            durationSeconds = domain.durationSeconds,
            startedAt = TimeConverter.toIso8601(domain.startedAt), // Long -> ISO String
            // paceSeconds 계산 로직 삭제 -> 서버에서 계산
            
            locations = flatLocations
        )
    }
}

