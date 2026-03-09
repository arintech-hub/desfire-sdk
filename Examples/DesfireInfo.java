package arin.testing;

/*
 * ================================================================
 * DESFireInfoDemo
 * ================================================================
 *
 * OBJETIVO:
 *   Demostrar lectura básica de información de una tarjeta
 *   MIFARE DESFire usando la librería incluida (DFCard + JSCIOComManager).
 *
 * FUNCIONES IMPLEMENTADAS:
 *   1) Listar lectores PC/SC
 *   2) Seleccionar lector PICC por índice
 *   3) Conectar a la tarjeta
 *   4) Ejecutar:
 *        - GetVersion()
 *        - GetApplicationIDs()
 *        - SelectApplication()
 *        - AuthenticateAES()
 *        - AuthenticateTDEA()
 *
 * IMPORTANTE:
 *   - Solo funciona si el reader ya está en modo ISO14443-4
 *
 * CAPAS INVOLUCRADAS:
 *   - PC/SC (winscard.dll)
 *   - JSCIOComManager (wrapper PC/SC)
 *   - DFCard (protocolo DESFire)
 *
 * ================================================================
 */

import DESFirepackage.library.CardType;
import DESFirepackage.library.DFCard;
import DESFirepackage.library.DFLException;
import DESFirepackage.library.DFResponse;
import DESFirepackage.library.middleware.ComManager;
import DESFirepackage.library.middleware.JSCIOComManager;
import DESFirepackage.library.param.AID;
import DESFirepackage.library.param.AIDS;
import DESFirepackage.library.param.PICCVersion;
import DESFirepackage.library.param.UID;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DesfireInfo {

    // --- Config ---
    private static final String FILTER = "PICC";
    private static final long WAIT_MS = 30_000;

    // PICC Master = 000000 (contexto PICC-level)
    private static final AID PICC_MASTER_AID = new AID(0x000000);

    // AES-128 key (16 bytes) para autenticar (ajustá KeyNo según tu tarjeta)
    private static final int KEY_NO = 0;
    private static final String AES_KEY_HEX = "00112233445566778899AABBCCDDEEFF";
    private static final String TDEA_KEY_HEX = "00000000000000000000000000000000";

    public static void main(String[] args) {

        /*
         * ComManager es la capa de comunicación PC/SC.
         * JSCIOComManager utiliza javax.smartcardio internamente.
         */
        ComManager cm = new JSCIOComManager();

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA");

        try {

            // ==========================================================
            // 1) Inicializar contexto PC/SC
            // ==========================================================
            cm.scan();  // Escanea y prepara contexto PC/SC

            // ==========================================================
            // 2) Listar lectores disponibles
            // ==========================================================
            String[] readers = cm.listReaders();

            if (readers.length == 0) {
                System.out.println("No PC/SC readers found.");
                return;
            }

            System.out.println("Available readers:");
            for (int i = 0; i < readers.length; i++) {
                System.out.println("[" + i + "] " + readers[i]);
            }
            System.out.println();

            // ==========================================================
            // 3) Filtrar lectores por "PICC" y seleccionar por índice
            //    (contactless interface)
            //    En la mayoría de los lectores no es por "PICC" sino "CL"
            // ==========================================================
            List<String> filtered = filterReaders(readers, FILTER);

            if (filtered.isEmpty()) {
                System.out.println("No readers matching " + FILTER + ". Using ALL readers.");
                filtered = List.of(readers);
            } else {
                System.out.println("Filtered readers (" + FILTER + "):");
                for (int i = 0; i < filtered.size(); i++) {
                    System.out.println("[" + i + "] " + filtered.get(i));
                }
                System.out.println();
            }

            int idx = 0; //askIndex("Select reader index: ", filtered.size());
            String readerName = filtered.get(idx);

            // ==========================================================
            // 4) Seleccionar lector
            // =================a=========================================
            cm.select(readerName);
            System.out.println("\nUsing: " + readerName);

            // ==========================================================
            // 5) Esperar presencia de tarjeta
            // ==========================================================
            waitForCardPresent(cm, WAIT_MS);

            // ==========================================================
            // 6) Conectar a la tarjeta
            // ==========================================================
            cm.connect();

            /*
             * CardType permite verificar si la librería detecta
             * que la tarjeta es DESFire.
             */
            CardType ct = cm.getCardType();
            System.out.println("CardType: " + ct);

            if (ct != CardType.MIFARE_DESFIRE) {
                System.out.println("WARNING: Card not detected as DESFire.");
            }

            // ==========================================================
            // 7) Crear objeto DFCard (protocolo DESFire)
            // ==========================================================
            DFCard df = new DFCard(cm);

            // ==========================================================
            // 8.1) GET VERSION
            // ==========================================================
            System.out.println("\n==== DESFire: GET VERSION ====");

            DFResponse verRes = df.getVersion();
            printStatus("GetVersion", verRes);

            /*
             * getVersion devuelve estructura PICCVersion
             * que incluye:
             *  - Hardware version
             *  - Software version
             *  - UID
             */
            try {
                PICCVersion pv = verRes.getPICCVersion();
                UID uidFromVersion = pv.getUID();

                System.out.println("UID (from GetVersion): " + uidFromVersion);
                System.out.println("PICCVersion: " + pv);

            } catch (Exception e) {
                System.out.println("Could not parse PICCVersion.");
            }

            // ==========================================================
            // 8.2) LIST APPLICATION IDs
            // ==========================================================
            System.out.println("\n==== DESFire: LIST APPLICATION IDs ====");

            DFResponse aidsRes = df.getApplicationIDs();
            printStatus("GetApplicationIDs", aidsRes);
            printAids(aidsRes);

            // ==========================================================
            // 8.3) SELECT PICC MASTER FILE (0x000000)
            // ==========================================================
            System.out.println("\n==== DESFire: SELECT APPLICATION (PICC MASTER 000000) ====");
            DFResponse sel = df.selectApplication(PICC_MASTER_AID);
            printStatus("SelectApplication(000000)", sel);

            // ==========================================================
            // 8.4) AUTHENTICATE AES (KeyNo + KeyBytes)
            // ==========================================================
            System.out.println("\n==== DESFire: AUTHENTICATE AES (KeyNo=" + KEY_NO + ") ====");
            byte[] key = hexToBytes(AES_KEY_HEX);

            DFResponse auth = df.authenticateAES(KEY_NO, key);

            printStatus("AuthenticateAES", auth);

            // ==========================================================
            // 8.5) AUTHENTICATE TDEA (KeyNo + KeyBytes)
            // ==========================================================
            System.out.println("\n==== DESFire: AUTHENTICATE TDEA (KeyNo=" + KEY_NO + ") ====");
            byte[] keyTDEA = hexToBytes(TDEA_KEY_HEX);

            DFResponse authTDEA = df.authenticate(KEY_NO, keyTDEA);

            printStatus("AuthenticateTDEA", authTDEA);

            // ==========================================================
            // 9) Cierre limpio de sesión
            // ==========================================================
            cm.disconnect();
            cm.deselect();
            cm.release();

            System.out.println("\nDONE.");

        } catch (DFLException e) {
            System.out.println("DFLException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }


    // ==========================================================
    // HELPERS
    // ==========================================================

    /*
     * Status printing (compatible with your DFResponse)
     */
    private static void printStatus(String op, DFResponse r) {
        System.out.println(op + " -> " + r.getSC() + " (ok=" + r.isOk() + ")");
    }

    /*
     * AIDs printing
     */
    private static void printAids(DFResponse aidsRes) {
        if (!aidsRes.isOk()) return;

        try {
            AIDS aids = aidsRes.getAIDs();
            AID[] list = aids.getAids();
            System.out.println("Applications found: " + list.length);
            for (int i = 0; i < list.length; i++) {
                System.out.println(" - [" + i + "] AID=" + list[i]);
            }
        } catch (Exception e) {
            System.out.println("No AIDs parsed/available in DFResponse.field.");
        }
    }

    /*
     * Filtra lectores que contengan cierto texto (ej: PICC).
     */
    private static List<String> filterReaders(String[] readers, String contains) {
        List<String> out = new ArrayList<>();
        for (String r : readers) {
            if (r != null && r.toUpperCase().contains(contains.toUpperCase())) {
                out.add(r);
            }
        }
        return out;
    }

    /*
     * Espera el tiempo determinado por presencia de tarjeta
     */
    private static void waitForCardPresent(ComManager cm, long timeoutMs) throws Exception {
        long t0 = System.currentTimeMillis();
        if (!cm.isCardPresent()) {
            System.out.println("Waiting for PICC... (bring card close)");
            while (!cm.isCardPresent()) {
                Thread.sleep(200);
                if (System.currentTimeMillis() - t0 > timeoutMs) {
                    throw new Exception("Timeout: no card present after " + timeoutMs + " ms");
                }
            }
        }
        System.out.println("PICC present.");
    }

    /*
     * Hex 2 byte converter
     */
    private static byte[] hexToBytes(String hex) {
        String h = hex.replace(" ", "").trim();
        if (h.length() % 2 != 0) throw new IllegalArgumentException("Hex length must be even");
        byte[] out = new byte[h.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(h.charAt(2 * i), 16);
            int lo = Character.digit(h.charAt(2 * i + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("Invalid hex: " + hex);
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}

