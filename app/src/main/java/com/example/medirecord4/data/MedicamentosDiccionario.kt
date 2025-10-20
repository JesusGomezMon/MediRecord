package com.example.medirecord4.data

/**
 * Diccionario de medicamentos comunes en español
 * con su descripción y tipo de venta
 */
object MedicamentosDiccionario {
    
    data class InfoMedicamento(
        val nombre: String,
        val descripcion: String,
        val ventaLibre: Boolean,
        val nombreGenerico: String = ""
    )
    
    private val medicamentos = mapOf(
        // Analgésicos y antiinflamatorios
        "paracetamol" to InfoMedicamento(
            "Paracetamol",
            "Analgésico y antipirético utilizado para aliviar el dolor leve a moderado y reducir la fiebre. Efectivo para dolores de cabeza, musculares, artritis menor, resfriados y dolor de muelas.",
            true,
            "Acetaminofén"
        ),
        "ibuprofeno" to InfoMedicamento(
            "Ibuprofeno",
            "Antiinflamatorio no esteroideo (AINE) usado para tratar el dolor, la fiebre y la inflamación. Útil para dolores menstruales, artritis, lesiones deportivas y dolores de cabeza.",
            true
        ),
        "aspirina" to InfoMedicamento(
            "Aspirina",
            "Analgésico, antipirético y antiinflamatorio. También se usa en bajas dosis para prevenir problemas cardiovasculares como infartos y derrames cerebrales.",
            true,
            "Ácido acetilsalicílico"
        ),
        "naproxeno" to InfoMedicamento(
            "Naproxeno",
            "Antiinflamatorio no esteroideo de acción prolongada. Se utiliza para aliviar el dolor y la inflamación causados por artritis, dolores menstruales y lesiones.",
            false
        ),
        
        // Antibióticos
        "amoxicilina" to InfoMedicamento(
            "Amoxicilina",
            "Antibiótico de amplio espectro del grupo de las penicilinas. Trata infecciones bacterianas como otitis, faringitis, neumonía, infecciones urinarias y de la piel.",
            false
        ),
        "azitromicina" to InfoMedicamento(
            "Azitromicina",
            "Antibiótico macrólido usado para tratar infecciones respiratorias, de oído, piel y enfermedades de transmisión sexual. Generalmente se toma por 3-5 días.",
            false
        ),
        "ciprofloxacino" to InfoMedicamento(
            "Ciprofloxacino",
            "Antibiótico fluoroquinolona de amplio espectro. Efectivo contra infecciones urinarias, respiratorias, gastrointestinales y de la piel.",
            false
        ),
        
        // Diabetes
        "metformina" to InfoMedicamento(
            "Metformina",
            "Medicamento antidiabético oral de primera línea para diabetes tipo 2. Reduce la producción de glucosa en el hígado y mejora la sensibilidad a la insulina.",
            false
        ),
        "glibenclamida" to InfoMedicamento(
            "Glibenclamida",
            "Antidiabético oral del grupo de las sulfonilureas. Estimula el páncreas para producir más insulina. Usado en diabetes tipo 2.",
            false
        ),
        
        // Cardiovasculares
        "losartan" to InfoMedicamento(
            "Losartán",
            "Antihipertensivo del grupo de los antagonistas de los receptores de angiotensina II (ARA-II). Reduce la presión arterial y protege los riñones en diabéticos.",
            false
        ),
        "enalapril" to InfoMedicamento(
            "Enalapril",
            "Inhibidor de la ECA usado para tratar la presión arterial alta e insuficiencia cardíaca. Ayuda a relajar los vasos sanguíneos.",
            false
        ),
        "atorvastatina" to InfoMedicamento(
            "Atorvastatina",
            "Estatina que reduce el colesterol LDL (malo) y los triglicéridos, mientras aumenta el colesterol HDL (bueno). Previene enfermedades cardiovasculares.",
            false
        ),
        
        // Gastrointestinales
        "omeprazol" to InfoMedicamento(
            "Omeprazol",
            "Inhibidor de la bomba de protones que reduce la producción de ácido estomacal. Trata úlceras, reflujo gastroesofágico y acidez estomacal.",
            false
        ),
        "ranitidina" to InfoMedicamento(
            "Ranitidina",
            "Antihistamínico H2 que reduce la producción de ácido estomacal. Usado para tratar úlceras y reflujo ácido.",
            true
        ),
        
        // Respiratorios
        "salbutamol" to InfoMedicamento(
            "Salbutamol",
            "Broncodilatador usado para aliviar los síntomas del asma y enfermedad pulmonar obstructiva crónica (EPOC). Relaja los músculos de las vías respiratorias.",
            false
        ),
        "loratadina" to InfoMedicamento(
            "Loratadina",
            "Antihistamínico de segunda generación para alergias. Alivia estornudos, picazón, ojos llorosos y secreción nasal sin causar mucha somnolencia.",
            true
        ),
        
        // Otros comunes
        "clonazepam" to InfoMedicamento(
            "Clonazepam",
            "Benzodiacepina usada para tratar trastornos de ansiedad, crisis de pánico y ciertos tipos de convulsiones. Tiene efecto sedante y relajante muscular.",
            false
        ),
        "diclofenaco" to InfoMedicamento(
            "Diclofenaco",
            "Antiinflamatorio no esteroideo potente. Alivia el dolor y la inflamación en artritis, lesiones deportivas, dolor postoperatorio y cólicos menstruales.",
            false
        ),
        "captopril" to InfoMedicamento(
            "Captopril",
            "Inhibidor de la ECA usado para tratar hipertensión arterial e insuficiencia cardíaca. Ayuda a los vasos sanguíneos a relajarse.",
            false
        )
    )
    
    /**
     * Busca información de un medicamento por su nombre
     */
    fun buscarMedicamento(nombre: String): InfoMedicamento? {
        val nombreLower = nombre.lowercase().trim()
        return medicamentos[nombreLower]
    }
    
    /**
     * Obtiene sugerencias de medicamentos similares
     */
    fun obtenerSugerencias(busqueda: String): List<String> {
        val busquedaLower = busqueda.lowercase().trim()
        return medicamentos.keys.filter { 
            it.contains(busquedaLower) || busquedaLower.contains(it)
        }.take(5)
    }
    
    /**
     * Obtiene todos los nombres de medicamentos disponibles
     */
    fun obtenerTodosMedicamentos(): List<String> {
        return medicamentos.keys.sorted()
    }
}

