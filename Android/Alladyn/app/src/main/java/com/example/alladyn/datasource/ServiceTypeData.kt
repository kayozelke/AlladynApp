package com.example.alladyn.datasource

import com.example.alladyn.model.ServiceTypeModel

class ServiceTypeData {
    fun loadServiceTypesData(): List<ServiceTypeModel>{
        return listOf(
            ServiceTypeModel("individual_service","Odbiór od klienta prywatnego", 13.0),
            ServiceTypeModel("partner_service","Odbiór od partnera firmy", 12.5)
        )
    }
}