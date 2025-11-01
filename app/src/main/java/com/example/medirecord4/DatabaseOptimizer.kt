package com.example.medirecord4

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Utilidad para optimizar y mantener la base de datos
 */
object DatabaseOptimizer {

    private const val TAG = "DatabaseOptimizer"

    /**
     * Optimiza la base de datos ejecutando VACUUM y ANALYZE
     */
    fun optimizeDatabase(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        try {
            // VACUUM libera espacio no utilizado y desfragmenta la base de datos
            db.execSQL("VACUUM")
            Log.d(TAG, "VACUUM ejecutado correctamente")
            
            // ANALYZE actualiza las estadísticas de la base de datos para mejorar el rendimiento de consultas
            db.execSQL("ANALYZE")
            Log.d(TAG, "ANALYZE ejecutado correctamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al optimizar base de datos: ${e.message}")
        } finally {
            db.close()
            dbHelper.close()
        }
    }

    /**
     * Limpia datos antiguos para mantener la base de datos ligera
     * - Historial de tomas mayor a 90 días
     * - Citas médicas pasadas mayor a 180 días
     * - Notificaciones antiguas mayor a 30 días
     */
    fun cleanOldData(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase
        
        try {
            db.beginTransaction()
            
            // Eliminar historial de tomas mayor a 90 días
            val deletedTomas = db.delete(
                "historial_tomas",
                "fecha_hora_toma < datetime('now', '-90 days')",
                null
            )
            Log.d(TAG, "Eliminadas $deletedTomas tomas antiguas")
            
            // Eliminar citas médicas completadas mayores a 180 días
            val deletedCitas = db.delete(
                "citas_medicas",
                "fecha_hora < datetime('now', '-180 days') AND asistio != 'pendiente'",
                null
            )
            Log.d(TAG, "Eliminadas $deletedCitas citas antiguas")
            
            // Eliminar notificaciones antiguas mayores a 30 días
            val deletedNotif = db.delete(
                "notificaciones",
                "fecha_envio < datetime('now', '-30 days') AND leida = 1",
                null
            )
            Log.d(TAG, "Eliminadas $deletedNotif notificaciones antiguas")
            
            db.setTransactionSuccessful()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar datos antiguos: ${e.message}")
        } finally {
            db.endTransaction()
            db.close()
            dbHelper.close()
        }
    }

    /**
     * Obtiene el tamaño de la base de datos en KB
     */
    fun getDatabaseSize(context: Context): Long {
        val dbPath = context.getDatabasePath("medirecord.db")
        return if (dbPath.exists()) {
            dbPath.length() / 1024 // Convertir a KB
        } else {
            0
        }
    }

    /**
     * Obtiene estadísticas de la base de datos
     */
    fun getDatabaseStats(context: Context): Map<String, Int> {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val stats = mutableMapOf<String, Int>()
        
        try {
            // Contar registros en cada tabla
            val tables = listOf(
                "medicamentos",
                "recordatorios",
                "historial_tomas",
                "citas_medicas",
                "notificaciones"
            )
            
            tables.forEach { table ->
                val cursor = db.rawQuery("SELECT COUNT(*) FROM $table", null)
                if (cursor.moveToFirst()) {
                    stats[table] = cursor.getInt(0)
                }
                cursor.close()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener estadísticas: ${e.message}")
        } finally {
            db.close()
            dbHelper.close()
        }
        
        return stats
    }

    /**
     * Verifica la integridad de la base de datos
     */
    fun checkDatabaseIntegrity(context: Context): Boolean {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase
        var isIntact = false
        
        try {
            val cursor = db.rawQuery("PRAGMA integrity_check", null)
            if (cursor.moveToFirst()) {
                val result = cursor.getString(0)
                isIntact = result == "ok"
                Log.d(TAG, "Integridad de BD: $result")
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar integridad: ${e.message}")
        } finally {
            db.close()
            dbHelper.close()
        }
        
        return isIntact
    }
}

