package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.PuntiVenditaService;
import com.twentyfive.twentyfivemodel.dto.partenupDto.Viaggio;
import com.twentyfive.twentyfivemodel.models.partenupModels.Cliente;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasporto;
import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettificaConValore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {
    private final TemplateEngine templateEngine = new TemplateEngine();

    private final PuntiVenditaService puntiVenditaService;


    private final String templatepreventivo = "preventivo.html";

    private final String templatetrasporto = "trasporto.html";

    @PostMapping("preventivo")
    public ResponseEntity<byte[]> stampapreventivo(@RequestBody Preventivo preventivo){

        Context ctx = new Context();

        compilacontextpreventivo(ctx,preventivo);

        String processedHtml = templateEngine.process(templatepreventivo, ctx);

        String filename = compilanomeptrventivo(preventivo);

        ByteArrayOutputStream os = null;

        byte[] result = {};

        try {
            //  final File outputFile = File.createTempFile(filename, ".pdf");
            os = new ByteArrayOutputStream();

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processedHtml);
            renderer.layout();
            renderer.createPDF(os, false);
            renderer.finishPDF();
            System.out.println("PDF creato");
            result = os.toByteArray();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Here you have to set the actual filename of your pdf

        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(result, headers, HttpStatus.OK);
        return response;
    }



    private String compilanomeptrventivo(Preventivo p){
        String nomecliente = p.getNomecliente();
        Date data = p.getRiferimento().getData();
        String nomepuntovendita = p.getRiferimento().getPuntoVendita().getNome();

        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy");

        StringBuilder result = new StringBuilder();
        result.append(nomecliente);
        result.append("_");
        result.append(nomepuntovendita);
        result.append("_");
        result.append(formatter.format(data));

        return result.toString();
    }


    private void compilacontextpreventivo(Context context, Preventivo preventivo){
        context.setVariable("nomecliente" , preventivo.getNomecliente());
        context.setVariable("nomepuntovendita" , preventivo.getRiferimento().getPuntoVendita().getNome());

        Date data = preventivo.getData();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        context.setVariable("data" , format.format(data));



        List<Rigapreventivo> listarighe = new LinkedList<>();

        Rigapreventivo riga1 = new Rigapreventivo();
        Cliente cliente = puntiVenditaService.cercaproprietario(preventivo.getRiferimento().getPuntoVendita());
        context.setVariable("pivacliente" , cliente.getPartitaiva());
        String indirizzo = preventivo.getRiferimento().getPuntoVendita().getVia();
        context.setVariable("indirizzo" , indirizzo);


        double gasolioself = preventivo.getPrezzoalpubblicogasolioself();
        double gasolioservito = preventivo.getPrezzoalpubblicogasolioservito();

        double benzinaself = preventivo.getPrezzoalpubblicobenzinaself();
        double benzinaservito = preventivo.getPrezzoalpubblicobenzinaservito();

        double supremeself = preventivo.getPrezzoalpubblicosupremeself();
        double supremeservito = preventivo.getPrezzoalpubblicosupremeservito();

        double gplservito = preventivo.getPrezzoalpubblicogplservito();

        context.setVariable("prezzogasolioself" , gasolioself);
        context.setVariable("prezzogasolioservito" , gasolioservito);
        context.setVariable("prezzobenzinaservito" , benzinaservito);
        context.setVariable("prezzobenzinaself" , benzinaself);
        context.setVariable("prezzosupremeself" , supremeself);
        context.setVariable("prezzosupremeservito" , supremeservito);
        context.setVariable("prezzogpl" , gplservito);



        double prezzolitro;

        if(preventivo.getRiferimento().getGasolio()>0){
            //GASOLIO
            riga1.descrizione = "GASOLIO";
            riga1.qta = preventivo.getRiferimento().getGasolio();
            prezzolitro = calcolaprezzodivendita(preventivo.getPrezzoalpubblicogasolioself(), cliente.getMarginegasolioself(), preventivo.getMarginecessionegasolio());
            prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.imposta = 22;
            riga1.valore = arrotondavalore(prezzolitro * riga1.qta);
            riga1.totale = arrotondavalore(riga1.valore * 1.22);
            listarighe.add(copiariga(riga1));
            riga1 = new Rigapreventivo();
        }

        if(preventivo.getRiferimento().getBenzina()>0){
            //GASOLIO
            riga1.descrizione = "BENZINA";
            riga1.qta = preventivo.getRiferimento().getBenzina();
            prezzolitro = calcolaprezzodivendita(preventivo.getPrezzoalpubblicobenzinaself(), cliente.getMarginebenzinaself(), preventivo.getMarginecessionebenzina());
            prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.imposta = 22;
            riga1.valore = arrotondavalore(prezzolitro * riga1.qta);
            riga1.totale = arrotondavalore(riga1.valore * 1.22);
            listarighe.add(copiariga(riga1));
            riga1 = new Rigapreventivo();
        }

        if(preventivo.getRiferimento().getSupreme()>0){
            //GASOLIO
            riga1.descrizione = "SUPREME";
            riga1.qta = preventivo.getRiferimento().getSupreme();
            prezzolitro = calcolaprezzodivendita(preventivo.getPrezzoalpubblicosupremeself(), cliente.getMarginesupremeself(), preventivo.getMarginecessionesupreme());
            prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.imposta = 22;
            riga1.valore = arrotondavalore(prezzolitro * riga1.qta);
            riga1.totale = arrotondavalore(riga1.valore * 1.22);
            listarighe.add(copiariga(riga1));
            riga1 = new Rigapreventivo();
        }

        if(preventivo.getRiferimento().getGpl()>0){
            //GASOLIO
            riga1.descrizione = "GPL";
            riga1.qta = preventivo.getRiferimento().getGpl();
            prezzolitro = calcolaprezzodivendita(preventivo.getPrezzoalpubblicogplservito(), cliente.getMarginegplservito(), preventivo.getMarginecessionegpl());
            prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.prezzolitro = arrotondaallaterzacifra(prezzolitro);
            riga1.imposta = 22;
            riga1.valore = arrotondavalore(prezzolitro * riga1.qta);
            riga1.totale = arrotondavalore(riga1.valore * 1.22);
            listarighe.add(copiariga(riga1));
        }



        context.setVariable("righepreventivo" , listarighe);

        double totalesenzaiva = calcolatotalepreventivosenzaiva(listarighe);
        double totale = calcolatotalepreventivo(listarighe);

        totale = arrotondavalore(totale);

        double totaleapagare = totale;

        for(VoceDiRettificaConValore voce : preventivo.getListavocidirettifica()){
            if(voce.isSegno()){
                totaleapagare += voce.getValore();
            }else{
                totaleapagare -= voce.getValore();
            }
        }

        totaleapagare = arrotondavalore(totaleapagare);

        context.setVariable("listavocidirettifica" , preventivo.getListavocidirettifica());

        context.setVariable("totalesenzaiva" , totalesenzaiva);
        context.setVariable("totale" , totale);
        context.setVariable("totaleapagare" , totaleapagare);



    }


    private double calcolatotalepreventivo(List<Rigapreventivo> listarighe){
        double totale = 0;
        for(Rigapreventivo riga : listarighe){
            totale += riga.totale;
        }
        return totale;
    }

    private double calcolatotalepreventivosenzaiva(List<Rigapreventivo> listarighe){
        double totale = 0;
        for(Rigapreventivo riga : listarighe){
            totale += riga.valore;
        }
        return totale;
    }

    private double calcolaprezzodivendita(double prezzoconiva, double margine, double percentuale){
        return calcolaprezzosenzaiva(prezzoconiva) - (margine*percentuale/100);
    }

    private double arrotondaallaterzacifra(double x){
        return (double)Math.round(x*1000)/1000;
    }

    private double arrotondavalore(double x){
        return (double)Math.round(x*100)/100;
    }

    private double calcolaprezzosenzaiva(double prezzo){
        return prezzo/1.22;
    }

    private Rigapreventivo copiariga(Rigapreventivo riga){
        Rigapreventivo result = new Rigapreventivo();
        result.totale = riga.totale;
        result.imposta = riga.imposta;
        result.prezzolitro = riga.prezzolitro;
        result.qta = riga.qta;
        result.descrizione = riga.descrizione;
        result.prezzototale = riga.prezzototale;
        result.valore = riga.valore;
        return result;
    }





    class Rigapreventivo{
        private double qta;
        private String descrizione;
        private double prezzolitro;
        private double prezzototale;
        private double imposta;
        private double totale;
        private double valore;

        public double getValore() {
            return valore;
        }

        public double getImposta() {
            return imposta;
        }

        public double getPrezzolitro() {
            return prezzolitro;
        }

        public double getPrezzototale() {
            return prezzototale;
        }

        public double getQta() {
            return qta;
        }

        public double getTotale() {
            return totale;
        }

        public String getDescrizione() {
            return descrizione;
        }

        public void setValore(double valore) {
            this.valore = valore;
        }

        public void setDescrizione(String descrizione) {
            this.descrizione = descrizione;
        }

        public void setImposta(double imposta) {
            this.imposta = imposta;
        }

        public void setPrezzolitro(double prezzolitro) {
            this.prezzolitro = prezzolitro;
        }

        public void setPrezzototale(double prezzototale) {
            this.prezzototale = prezzototale;
        }

        public void setQta(double qta) {
            this.qta = qta;
        }

        public void setTotale(double totale) {
            this.totale = totale;
        }
    }





    @PostMapping("trasporto")
    public ResponseEntity<byte[]> stampapreventivo(@RequestBody Viaggio viaggio){
        Context ctx = new Context();
        compilacontexttrasporto(ctx,viaggio);
        String processedHtml = templateEngine.process(templatetrasporto, ctx);
        String filename = "Trasporto";


        ByteArrayOutputStream os = null;
        byte[] result = {};

        try {
            //  final File outputFile = File.createTempFile(filename, ".pdf");
            os = new ByteArrayOutputStream();

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processedHtml);
            renderer.layout();
            renderer.createPDF(os, false);
            renderer.finishPDF();
            System.out.println("PDF creato");
            result = os.toByteArray();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Here you have to set the actual filename of your pdf

        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(result, headers, HttpStatus.OK);
        return response;
    }


    private void compilacontexttrasporto(Context context, Viaggio viaggio){

        Date datacaricazione = new Date();
        String deposito = "";
        String nometrasportatore = "";
        String targaatk = "";
        String targarimorchio = "";
        String nomeautista = "";
        List<RigaTrasporto> righetrasporto = new LinkedList<>();
        Cliente c;

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        for(Trasporto t : viaggio.getListaviaggi()){
            datacaricazione = t.getDatadicaricazione();
            deposito = t.getFabbisogno().getBasedicarico().getNomebasedicarico();
            nometrasportatore = t.getNometrasportatore();
            targaatk = t.getAtk().getTarga();
            targarimorchio = t.getRimorchio().getTarga();
            nomeautista = t.getAutista().getNomeautista();


            c = puntiVenditaService.cercaproprietario(t.getFabbisogno().getPuntoVendita());


            RigaTrasporto rigacurr = new RigaTrasporto();
            rigacurr.setCodicedestinazione(t.getFabbisogno().getPuntoVendita().getCodicedestinazione());
            rigacurr.setNomepuntovendita(c.getNomecliente());
            rigacurr.setPartitaiva(c.getPartitaiva());
            rigacurr.setBenzina(t.getFabbisogno().getBenzina());
            rigacurr.setGasolio(t.getFabbisogno().getGasolio());
            rigacurr.setSupreme(t.getFabbisogno().getSupreme());

            righetrasporto.add(rigacurr);
        }


        context.setVariable("deposito" , deposito);
        context.setVariable("datacaricazione" , format.format(datacaricazione));
        context.setVariable("nometrasportatore" , nometrasportatore);
        context.setVariable("targaatk" , targaatk);
        context.setVariable("targarimorchio" , targarimorchio);
        context.setVariable("nomeautista" , nomeautista);
        context.setVariable("righetrasporto" , righetrasporto);

    }


    class RigaTrasporto {
        private String codicedestinazione;
        private String nomepuntovendita;
        private String partitaiva;
        private double benzina;
        private double gasolio;
        private double supreme;

        public double getSupreme() {
            return supreme;
        }

        public double getGasolio() {
            return gasolio;
        }

        public double getBenzina() {
            return benzina;
        }

        public String getPartitaiva() {
            return partitaiva;
        }

        public String getCodicedestinazione() {
            return codicedestinazione;
        }

        public String getNomepuntovendita() {
            return nomepuntovendita;
        }

        public void setSupreme(double supreme) {
            this.supreme = supreme;
        }

        public void setGasolio(double gasolio) {
            this.gasolio = gasolio;
        }

        public void setBenzina(double benzina) {
            this.benzina = benzina;
        }

        public void setPartitaiva(String partitaiva) {
            this.partitaiva = partitaiva;
        }

        public void setCodicedestinazione(String codicedestinazione) {
            this.codicedestinazione = codicedestinazione;
        }

        public void setNomepuntovendita(String nomepuntovendita) {
            this.nomepuntovendita = nomepuntovendita;
        }


    }
}
