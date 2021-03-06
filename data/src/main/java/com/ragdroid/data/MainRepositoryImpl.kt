package com.ragdroid.data

import com.ragdroid.api.MarvelApi
import com.ragdroid.api.entity.TCharacterMarvel
import com.ragdroid.api.entity.TDataWrapper
import com.ragdroid.data.base.Helpers
import com.ragdroid.data.entity.AppConfig
import com.ragdroid.data.entity.CharacterMapper
import com.ragdroid.data.entity.CharacterMarvel
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by garimajain on 18/11/17.
 */
class MainRepositoryImpl
@Inject
constructor(
        private val marvelApi: MarvelApi,
        private val characterMapper: CharacterMapper,
        private val config: AppConfig,
        private val helpers: Helpers): MainRepository {


    override fun fetchCharacters(): Single<List<CharacterMarvel>> {
        val timeStamp = System.currentTimeMillis()
        return charactersApiSingle(timeStamp)
                .map { dataWrapper: TDataWrapper<List<TCharacterMarvel>> ->
                    return@map dataWrapper.data.results
                            .map {
                                characterMapper.map(it)
                            }.toList()
                }
    }

    override fun fetchCharacter(id: Long): Single<CharacterMarvel> {
        val timeStamp = System.currentTimeMillis()
        return characterApiSingle(id, timeStamp)
                .map { dataWrapper: TDataWrapper<List<TCharacterMarvel>> ->
                    return@map dataWrapper.data.results
                            .map {
                                characterMapper.map(it)
                            }.getOrNull(0)
                }.map {
                    it ?: throw IllegalAccessException("Character for id $id not found")
                }
    }

    private fun characterApiSingle(id: Long, timeStamp: Long): Single<TDataWrapper<List<TCharacterMarvel>>> {
        return marvelApi.getCharacter(
                id,
                config.publicKey,
                helpers.buildMD5Digest("" + timeStamp + config.privateKey
                        + config.publicKey),
                timeStamp
        )
    }
    private fun charactersApiSingle(timeStamp: Long): Single<TDataWrapper<List<TCharacterMarvel>>> {
        return marvelApi.getCharacters(
                config.publicKey,
                helpers.buildMD5Digest("" + timeStamp + config.privateKey
                        + config.publicKey),
                timeStamp,
                0,
                50
        )
    }

}

interface MainRepository {

    fun fetchCharacters(): Single<List<CharacterMarvel>>
    fun fetchCharacter(id: Long): Single<CharacterMarvel>
}
