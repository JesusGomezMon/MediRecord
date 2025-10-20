package com.example.medirecord4

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "medirecord.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla usuarios
        db.execSQL("""
            CREATE TABLE usuarios (
                id TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                telefono TEXT,
                fecha_nacimiento TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)

        // Crear tabla perfiles_medicos
        db.execSQL("""
            CREATE TABLE perfiles_medicos (
                id TEXT PRIMARY KEY,
                usuario_id TEXT NOT NULL,
                tipo_sangre TEXT CHECK(tipo_sangre IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')),
                alergias TEXT,
                condiciones_medicas TEXT,
                contacto_emergencia_nombre TEXT,
                contacto_emergencia_telefono TEXT,
                notas_medicas TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """)

        // Crear tabla medicamentos
        db.execSQL("""
            CREATE TABLE medicamentos (
                id TEXT PRIMARY KEY,
                usuario_id TEXT NOT NULL,
                nombre TEXT NOT NULL,
                dosis TEXT NOT NULL,
                tipo TEXT CHECK(tipo IN ('tableta', 'capsula', 'liquido', 'inyeccion', 'crema', 'otro')),
                via_administracion TEXT CHECK(via_administracion IN ('oral', 'topico', 'inyeccion', 'inhalacion', 'otro')),
                instrucciones TEXT,
                stock_actual INTEGER DEFAULT 0,
                stock_total INTEGER DEFAULT 0,
                activo INTEGER DEFAULT 1,
                tipo_tratamiento TEXT CHECK(tipo_tratamiento IN ('temporal', 'permanente')) DEFAULT 'temporal',
                duracion_dias INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """)

        // Crear tabla recordatorios
        db.execSQL("""
            CREATE TABLE recordatorios (
                id TEXT PRIMARY KEY,
                medicamento_id TEXT NOT NULL,
                hora TEXT NOT NULL,
                dias_semana TEXT,
                fecha_inicio TEXT NOT NULL,
                fecha_fin TEXT,
                activo INTEGER DEFAULT 1,
                sonido_alarma TEXT DEFAULT 'default',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id) ON DELETE CASCADE
            )
        """)

        // Crear tabla historial_tomas
        db.execSQL("""
            CREATE TABLE historial_tomas (
                id TEXT PRIMARY KEY,
                recordatorio_id TEXT NOT NULL,
                medicamento_id TEXT NOT NULL,
                usuario_id TEXT NOT NULL,
                fecha_hora_programada DATETIME NOT NULL,
                fecha_hora_toma DATETIME,
                estado TEXT CHECK(estado IN ('pendiente', 'tomado', 'omitido', 'retrasado')) DEFAULT 'pendiente',
                notas TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (recordatorio_id) REFERENCES recordatorios(id) ON DELETE CASCADE,
                FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id) ON DELETE CASCADE,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """)

        // Crear tabla citas_medicas
        db.execSQL("""
            CREATE TABLE citas_medicas (
                id TEXT PRIMARY KEY,
                usuario_id TEXT NOT NULL,
                doctor_nombre TEXT NOT NULL,
                especialidad TEXT,
                fecha_hora DATETIME NOT NULL,
                ubicacion TEXT,
                notas TEXT,
                recordatorio_enviado INTEGER DEFAULT 0,
                asistio TEXT CHECK(asistio IN ('pendiente', 'asistio', 'no_asistio')) DEFAULT 'pendiente',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """)

        // Crear tabla interacciones_medicamentosas
        db.execSQL("""
            CREATE TABLE interacciones_medicamentosas (
                id TEXT PRIMARY KEY,
                usuario_id TEXT NOT NULL,
                medicamento1_id TEXT NOT NULL,
                medicamento2_id TEXT NOT NULL,
                nivel_gravedad TEXT CHECK(nivel_gravedad IN ('leve', 'moderado', 'grave')) NOT NULL,
                descripcion TEXT NOT NULL,
                recomendaciones TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                FOREIGN KEY (medicamento1_id) REFERENCES medicamentos(id) ON DELETE CASCADE,
                FOREIGN KEY (medicamento2_id) REFERENCES medicamentos(id) ON DELETE CASCADE
            )
        """)

        // Crear tabla notificaciones
        db.execSQL("""
            CREATE TABLE notificaciones (
                id TEXT PRIMARY KEY,
                usuario_id TEXT NOT NULL,
                titulo TEXT NOT NULL,
                mensaje TEXT NOT NULL,
                tipo TEXT CHECK(tipo IN ('recordatorio', 'alerta', 'informacion')) NOT NULL,
                leida INTEGER DEFAULT 0,
                fecha_envio DATETIME NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """)

        // Crear indices
        db.execSQL("CREATE INDEX idx_medicamentos_usuario ON medicamentos(usuario_id)")
        db.execSQL("CREATE INDEX idx_recordatorios_medicamento ON recordatorios(medicamento_id)")
        db.execSQL("CREATE INDEX idx_historial_usuario_fecha ON historial_tomas(usuario_id, fecha_hora_programada)")
        db.execSQL("CREATE INDEX idx_citas_usuario_fecha ON citas_medicas(usuario_id, fecha_hora)")
        db.execSQL("CREATE INDEX idx_notificaciones_usuario_fecha ON notificaciones(usuario_id, fecha_envio)")

        // Insertar datos de ejemplo
        insertarDatosEjemplo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Eliminar tablas existentes
        db.execSQL("DROP TABLE IF EXISTS notificaciones")
        db.execSQL("DROP TABLE IF EXISTS interacciones_medicamentosas")
        db.execSQL("DROP TABLE IF EXISTS citas_medicas")
        db.execSQL("DROP TABLE IF EXISTS historial_tomas")
        db.execSQL("DROP TABLE IF EXISTS recordatorios")
        db.execSQL("DROP TABLE IF EXISTS medicamentos")
        db.execSQL("DROP TABLE IF EXISTS perfiles_medicos")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        
        // Recrear base de datos
        onCreate(db)
    }

    private fun insertarDatosEjemplo(db: SQLiteDatabase) {
        // Insertar usuarios
        db.execSQL("""
            INSERT INTO usuarios (id, nombre, email, telefono, fecha_nacimiento) VALUES 
            ('usr-001', 'Maria Gonzalez', 'maria.gonzalez@email.com', '+525512345678', '1958-03-15'),
            ('usr-002', 'Carlos Lopez', 'carlos.lopez@email.com', '+525598765432', '1945-07-22')
        """)

        // Insertar perfiles medicos
        db.execSQL("""
            INSERT INTO perfiles_medicos (id, usuario_id, tipo_sangre, alergias, condiciones_medicas, contacto_emergencia_nombre, contacto_emergencia_telefono) VALUES 
            ('perf-001', 'usr-001', 'O+', 'Penicilina, Mariscos', 'Hipertension, Diabetes tipo 2', 'Juan Gonzalez', '+525511223344'),
            ('perf-002', 'usr-002', 'A-', 'Nueces', 'Artritis, Colesterol alto', 'Ana Lopez', '+525566778899')
        """)

        // Insertar medicamentos
        db.execSQL("""
            INSERT INTO medicamentos (id, usuario_id, nombre, dosis, tipo, via_administracion, instrucciones, stock_actual, stock_total, tipo_tratamiento, duracion_dias) VALUES 
            ('med-001', 'usr-001', 'Metformina', '500mg', 'tableta', 'oral', 'Tomar con alimentos', 28, 30, 'permanente', 0),
            ('med-002', 'usr-001', 'Losartan', '50mg', 'tableta', 'oral', 'Tomar en ayunas', 15, 30, 'permanente', 0),
            ('med-003', 'usr-001', 'Atorvastatina', '20mg', 'tableta', 'oral', 'Tomar en la noche', 10, 30, 'permanente', 0),
            ('med-004', 'usr-002', 'Ibuprofeno', '400mg', 'tableta', 'oral', 'Tomar con alimentos', 25, 30, 'temporal', 7)
        """)

        // Insertar recordatorios
        db.execSQL("""
            INSERT INTO recordatorios (id, medicamento_id, hora, dias_semana, fecha_inicio, fecha_fin) VALUES 
            ('rec-001', 'med-001', '08:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', '2024-01-01', NULL),
            ('rec-002', 'med-001', '20:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', '2024-01-01', NULL),
            ('rec-003', 'med-002', '07:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', '2024-01-01', NULL),
            ('rec-004', 'med-003', '22:00:00', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', '2024-01-01', NULL),
            ('rec-005', 'med-004', '08:00:00', 'LUNES,MIERCOLES,VIERNES', '2024-01-01', NULL)
        """)

        // Insertar historial de tomas
        db.execSQL("""
            INSERT INTO historial_tomas (id, recordatorio_id, medicamento_id, usuario_id, fecha_hora_programada, fecha_hora_toma, estado) VALUES 
            ('hist-001', 'rec-001', 'med-001', 'usr-001', '2024-01-15 08:00:00', '2024-01-15 08:05:00', 'tomado'),
            ('hist-002', 'rec-002', 'med-001', 'usr-001', '2024-01-15 20:00:00', '2024-01-15 20:30:00', 'tomado'),
            ('hist-003', 'rec-001', 'med-001', 'usr-001', '2024-01-16 08:00:00', NULL, 'omitido'),
            ('hist-004', 'rec-002', 'med-001', 'usr-001', '2024-01-16 20:00:00', '2024-01-16 21:15:00', 'retrasado')
        """)

        // Insertar citas medicas
        db.execSQL("""
            INSERT INTO citas_medicas (id, usuario_id, doctor_nombre, especialidad, fecha_hora, ubicacion) VALUES 
            ('cita-001', 'usr-001', 'Dr. Roberto Mendoza', 'Endocrinologia', '2024-02-01 10:00:00', 'Hospital Central, Consultorio 305'),
            ('cita-002', 'usr-002', 'Dra. Laura Sanchez', 'Reumatologia', '2024-02-05 11:30:00', 'Clinica del Norte, Piso 2')
        """)

        // Insertar interacciones medicamentosas
        db.execSQL("""
            INSERT INTO interacciones_medicamentosas (id, usuario_id, medicamento1_id, medicamento2_id, nivel_gravedad, descripcion, recomendaciones) VALUES 
            ('int-001', 'usr-001', 'med-001', 'med-002', 'leve', 'Puede aumentar el efecto de la metformina', 'Monitorear niveles de glucosa con frecuencia')
        """)

        // Insertar notificaciones
        db.execSQL("""
            INSERT INTO notificaciones (id, usuario_id, titulo, mensaje, tipo, fecha_envio) VALUES 
            ('not-001', 'usr-001', 'Recordatorio de Medicamento', 'Es hora de tomar Metformina 500mg', 'recordatorio', '2024-01-17 08:00:00'),
            ('not-002', 'usr-001', 'Stock Bajo', 'Solo te quedan 10 tabletas de Atorvastatina', 'alerta', '2024-01-17 09:00:00')
        """)
    }

    // Métodos helper para consultas comunes
    
    fun obtenerTodosLosMedicamentos(usuarioId: String): List<Map<String, String>> {
        val medicamentos = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, nombre, dosis, tipo FROM medicamentos WHERE usuario_id = ? AND activo = 1",
            arrayOf(usuarioId)
        )
        
        while (cursor.moveToNext()) {
            medicamentos.add(mapOf(
                "id" to cursor.getString(0),
                "nombre" to cursor.getString(1),
                "dosis" to cursor.getString(2),
                "tipo" to cursor.getString(3)
            ))
        }
        cursor.close()
        return medicamentos
    }

    fun obtenerRecordatoriosHoy(): List<Map<String, String>> {
        val recordatorios = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT 
                r.id,
                m.nombre as medicamento,
                m.dosis,
                r.hora,
                u.nombre as usuario
            FROM recordatorios r
            JOIN medicamentos m ON r.medicamento_id = m.id
            JOIN usuarios u ON m.usuario_id = u.id
            WHERE r.activo = 1
            AND (r.fecha_fin IS NULL OR date('now') <= r.fecha_fin)
        """, null)
        
        while (cursor.moveToNext()) {
            recordatorios.add(mapOf(
                "id" to cursor.getString(0),
                "medicamento" to cursor.getString(1),
                "dosis" to cursor.getString(2),
                "hora" to cursor.getString(3),
                "usuario" to cursor.getString(4)
            ))
        }
        cursor.close()
        return recordatorios
    }

    fun obtenerEstadisticasCumplimiento(usuarioId: String): List<Map<String, String>> {
        val estadisticas = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT 
                m.nombre as medicamento,
                COUNT(*) as total_recordatorios,
                SUM(CASE WHEN ht.estado = 'tomado' THEN 1 ELSE 0 END) as tomas_realizadas,
                ROUND((SUM(CASE WHEN ht.estado = 'tomado' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 2) as porcentaje_cumplimiento
            FROM historial_tomas ht
            JOIN medicamentos m ON ht.medicamento_id = m.id
            WHERE ht.usuario_id = ?
            GROUP BY m.id
        """, arrayOf(usuarioId))
        
        while (cursor.moveToNext()) {
            estadisticas.add(mapOf(
                "medicamento" to cursor.getString(0),
                "total" to cursor.getString(1),
                "realizadas" to cursor.getString(2),
                "porcentaje" to cursor.getString(3)
            ))
        }
        cursor.close()
        return estadisticas
    }

    fun obtenerMedicamentosStockBajo(usuarioId: String): List<Map<String, String>> {
        val medicamentos = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT 
                m.nombre as medicamento,
                m.stock_actual,
                m.stock_total,
                ROUND((m.stock_actual * 100.0 / m.stock_total), 2) as porcentaje_stock
            FROM medicamentos m
            WHERE m.usuario_id = ?
            AND m.stock_actual < (m.stock_total * 0.25)
            AND m.activo = 1
        """, arrayOf(usuarioId))
        
        while (cursor.moveToNext()) {
            medicamentos.add(mapOf(
                "medicamento" to cursor.getString(0),
                "stock_actual" to cursor.getString(1),
                "stock_total" to cursor.getString(2),
                "porcentaje" to cursor.getString(3)
            ))
        }
        cursor.close()
        return medicamentos
    }

    /**
     * Verifica si un medicamento temporal ha completado su tratamiento
     * y lo desactiva automáticamente
     */
    fun verificarYDesactivarTratamientoCompleto(medicamentoId: String, usuarioId: String) {
        val db = writableDatabase
        
        // Obtener información del medicamento
        val cursor = db.rawQuery("""
            SELECT tipo_tratamiento, duracion_dias, created_at
            FROM medicamentos
            WHERE id = ? AND usuario_id = ? AND activo = 1
        """, arrayOf(medicamentoId, usuarioId))
        
        if (cursor.moveToFirst()) {
            val tipoTratamiento = cursor.getString(0)
            val duracionDias = cursor.getInt(1)
            val fechaInicio = cursor.getString(2)
            
            // Solo verificar si es tratamiento temporal
            if (tipoTratamiento == "temporal" && duracionDias > 0) {
                // Contar cuántas tomas se han realizado
                val cursorTomas = db.rawQuery("""
                    SELECT COUNT(*) 
                    FROM historial_tomas 
                    WHERE medicamento_id = ? 
                    AND usuario_id = ? 
                    AND estado = 'tomado'
                """, arrayOf(medicamentoId, usuarioId))
                
                if (cursorTomas.moveToFirst()) {
                    val tomasRealizadas = cursorTomas.getInt(0)
                    
                    // Contar cuántas tomas totales debería tener el tratamiento
                    val cursorRecordatorios = db.rawQuery("""
                        SELECT COUNT(*) 
                        FROM recordatorios 
                        WHERE medicamento_id = ? AND activo = 1
                    """, arrayOf(medicamentoId))
                    
                    if (cursorRecordatorios.moveToFirst()) {
                        val recordatoriosPorDia = cursorRecordatorios.getInt(0)
                        val tomasTotales = recordatoriosPorDia * duracionDias
                        
                        // Calcular porcentaje de cumplimiento
                        val porcentajeCumplimiento = if (tomasTotales > 0) {
                            (tomasRealizadas * 100.0 / tomasTotales)
                        } else {
                            0.0
                        }
                        
                        // Si llegó al 100% o más, desactivar medicamento
                        if (porcentajeCumplimiento >= 100.0) {
                            db.execSQL("""
                                UPDATE medicamentos 
                                SET activo = 0, updated_at = CURRENT_TIMESTAMP 
                                WHERE id = ? AND usuario_id = ?
                            """, arrayOf(medicamentoId, usuarioId))
                            
                            // Desactivar también los recordatorios asociados
                            db.execSQL("""
                                UPDATE recordatorios 
                                SET activo = 0 
                                WHERE medicamento_id = ?
                            """, arrayOf(medicamentoId))
                        }
                    }
                    cursorRecordatorios.close()
                }
                cursorTomas.close()
            }
        }
        cursor.close()
    }

    /**
     * Obtiene el tipo de tratamiento y progreso de un medicamento
     */
    fun obtenerProgresoMedicamento(medicamentoId: String, usuarioId: String): Map<String, Any> {
        val db = readableDatabase
        val resultado = mutableMapOf<String, Any>()
        
        val cursor = db.rawQuery("""
            SELECT m.nombre, m.tipo_tratamiento, m.duracion_dias,
                   (SELECT COUNT(*) FROM historial_tomas 
                    WHERE medicamento_id = m.id AND estado = 'tomado') as tomas_realizadas,
                   (SELECT COUNT(*) FROM recordatorios 
                    WHERE medicamento_id = m.id AND activo = 1) as recordatorios_dia
            FROM medicamentos m
            WHERE m.id = ? AND m.usuario_id = ?
        """, arrayOf(medicamentoId, usuarioId))
        
        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(0)
            val tipoTratamiento = cursor.getString(1)
            val duracionDias = cursor.getInt(2)
            val tomasRealizadas = cursor.getInt(3)
            val recordatoriosDia = cursor.getInt(4)
            
            val tomasTotales = recordatoriosDia * duracionDias
            val porcentaje = if (tomasTotales > 0) {
                (tomasRealizadas * 100.0 / tomasTotales).coerceAtMost(100.0)
            } else {
                0.0
            }
            
            resultado["nombre"] = nombre
            resultado["tipo"] = tipoTratamiento
            resultado["progreso"] = String.format("%.1f", porcentaje)
            resultado["tomas_realizadas"] = tomasRealizadas
            resultado["tomas_totales"] = tomasTotales
            resultado["es_permanente"] = tipoTratamiento == "permanente"
        }
        
        cursor.close()
        return resultado
    }
}


