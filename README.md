# desfire-sdk

## Descripción

`DesfireInfo` es una clase de demostración utilizada para **probar el funcionamiento básico de la librería `desfire-sdk`** y verificar la comunicación con una tarjeta **MIFARE DESFire** a través de un lector **PC/SC**.

El objetivo principal es validar que:

- El lector PC/SC es detectado correctamente
- La tarjeta DESFire puede ser seleccionada
- La librería puede ejecutar comandos básicos del protocolo DESFire

Esta clase funciona como **programa de diagnóstico o smoke test** para confirmar que el entorno de desarrollo y la librería están configurados correctamente.


# Objetivo

Proveer una prueba mínima de lectura de información de una tarjeta **MIFARE DESFire**, utilizando las capas de comunicación incluidas en la librería.

El programa permite verificar rápidamente:

- Comunicación PC/SC
- Detección del lector
- Conexión con la tarjeta
- Ejecución de comandos básicos DESFire

---

# Funcionalidades

La clase implementa las siguientes operaciones:

1. **Listar lectores PC/SC disponibles**
2. **Seleccionar un lector compatible con PICC**
3. **Esperar la presencia de una tarjeta**
4. **Conectarse a la tarjeta**
5. Ejecutar comandos de consulta:

| Comando | Descripción |
|--------|-------------|
| `GetVersion()` | Obtiene la versión del chip DESFire |
| `GetApplicationIDs()` | Lista las aplicaciones presentes en la tarjeta |

Estas operaciones permiten confirmar que la comunicación con la tarjeta está funcionando correctamente.

---

# Qué NO hace esta clase

Este ejemplo **no implementa funcionalidades completas de seguridad ni gestión de aplicaciones**.

Limitaciones:

- No modifica contenido de la tarjeta
- No crea ni elimina aplicaciones
- No gestiona archivos DESFire

Su propósito es **únicamente diagnóstico y validación de la librería**.

---

# Arquitectura de capas

La comunicación se realiza a través de las siguientes capas:
 
- PC/SC (winscard.dll)
- JSCIOComManager (wrapper PC/SC)
- DFCard (protocolo DESFire)

---

# Dependencias

El programa utiliza clases incluidas en la librería:

- DESFirepackage.library.* 
- DESFirepackage.library.middleware.*
- DESFirepackage.library.param.*

Principales clases utilizadas:

| Clase | Función |
|------|--------|
| `DFCard` | Implementa el protocolo DESFire |
| `ComManager` | Interfaz de comunicación con el lector |
| `JSCIOComManager` | Implementación PC/SC |
| `PICCVersion` | Información de versión del chip |
| `AID` / `AIDS` | Identificadores de aplicaciones |

---

# Requisitos

Para ejecutar el programa se requiere:

- Java 17 o superior
- Lector NFC compatible con **PC/SC**
- Drivers del lector instalados
- Una tarjeta **MIFARE DESFire**

---

# Ejecución

1. Conectar el lector NFC al sistema
2. Colocar una tarjeta DESFire sobre el lector
3. Ejecutar el programa

El sistema:

- detectará los lectores
- seleccionará el lector PICC
- se conectará a la tarjeta
- mostrará información básica

---

# Uso típico

Este programa se utiliza para:

- Validar instalación de la librería `desfire-sdk`
- Verificar comunicación con lectores NFC
- Confirmar compatibilidad del entorno PC/SC
- Probar tarjetas DESFire en desarrollo

---

# Autor

Ejemplo de diagnóstico incluido con la librería **desfire-sdk**.