package com.cs4347.drumkit.transmission

import io.reactivex.subjects.PublishSubject
import Sensor.WatchPacket.SensorMessage
import io.reactivex.Observable

/**
 * Manually coded singleton for flexibility
 * Sensor data observers should observe this subject
 */
class SensorDataSubject private constructor() {
    private var subject: PublishSubject<SensorMessage> = PublishSubject.create()

    val serviceConnectionListener: ServiceConnectionListener =
            object: ServiceConnectionListener {
                override fun onInit() {
                    // reset after init doesn't matter, there should not be observers
                    reset()
                }

                override fun onReceive(packet: Sensor.WatchPacket) {
                    // TODO: for debugging, delete before submission
                    val firstMsg = packet.getMessages(0)
                    Log.d(TAG, "Packet's First Msg: ${firstMsg.sensorType}, " +
                            "Time: ${firstMsg.timestamp}, " +
                            "Data: ${firstMsg.dataList}")
                    packet.messagesList.forEach {
                        subject.onNext(it)
                    }
                }

                override fun onConnectionLost() {
                    subject.onError(Exception("Connection lost"))
                }
            }

    companion object {
        val instance = SensorDataSubject()
        private val TAG = "SensorDataSubject"
    }

    private fun reset() {
        if (!subject.hasComplete() || !subject.hasThrowable()) {
            subject.onError(Exception("SensorDataSubject is abruptly reset"))
        }
        subject = PublishSubject.create()
    }

    fun observe(): Observable<SensorMessage> {
        return subject
    }

}