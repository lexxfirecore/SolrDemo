package com.lexx;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import static org.junit.Assert.assertEquals;

/**
 * Created by alexandruco on 13-May-19.
 */
public class SolrDemo {
    public static void main(String[] args) throws IOException, SolrServerException {
        System.out.println(SolrDemo.class.getName());

        String urlString = "http://localhost:8983/solr/Document";
        HttpSolrClient solr = createSolrClient(urlString);

        Product product1 = new Product("123456","Kenmore Dishwasher","599.99");
        solrAdd(solr, product1);

        Product product2 = new Product("888", "Apple iPhone 6s", "299.99");
        solrAddBean(solr, product2);


        solrQuery(solr, product1);

        solrGetById(solr, product1);

        deleteById(solr, product1);

        deleteByQuery(solr, product1);
    }

    private static HttpSolrClient createSolrClient(String urlString) {
        System.out.println("createSolrClient");
        HttpSolrClient solr = new HttpSolrClient.Builder(urlString).build();
        solr.setParser(new XMLResponseParser());
        return solr;
    }

    private static void solrAdd(HttpSolrClient solr, Product product) throws SolrServerException, IOException {
        System.out.println("solrAdd");

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", product.getId());
        document.addField("name", product.getName());
        document.addField("price", product.getPrice());

        solr.add(document);
        solr.commit();
    }

    private static void solrAddBean(HttpSolrClient solr, Product product) throws IOException, SolrServerException {
        System.out.println("solrAddBean");

        solr.addBean(product);
        solr.commit();
    }

    private static void solrQuery(HttpSolrClient solr, Product product) throws IOException, SolrServerException {
        System.out.println("solrQuery");

        SolrQuery query = new SolrQuery();
        query.set("q", "price:" + product.getPrice());
        QueryResponse response = solr.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(docList.getNumFound(), 1);

        for (SolrDocument doc : docList) {
            assertEquals((String) doc.getFieldValue("id"), product.getId());
            assertEquals((Double) doc.getFieldValue("price"), Double.valueOf(product.getPrice()));
        }
    }

    private static void solrGetById(HttpSolrClient solr, Product product) throws IOException, SolrServerException {
        System.out.println("solrGetById");

        SolrDocument doc = solr.getById(product.getId());
        assertEquals((String) doc.getFieldValue("name"), product.getName());
        assertEquals((Double) doc.getFieldValue("price"), Double.valueOf(product.getPrice()));
    }

    private static void deleteById(HttpSolrClient solr, Product product) throws IOException, SolrServerException {
        System.out.println("deleteById");

        solr.deleteById(product.getId());
        solr.commit();

        SolrQuery query = new SolrQuery();
        query.set("q", "id:"+ product.getId());
        QueryResponse response = solr.query(query);
        SolrDocumentList documentList = response.getResults();
        assertEquals(documentList.getNumFound(), 0);
    }

    private static void deleteByQuery(HttpSolrClient solr, Product product) throws IOException, SolrServerException {
        System.out.println("deleteByQuery");

        solr.deleteByQuery("name:" + product.getName());
        solr.commit();

        SolrQuery query = new SolrQuery();
        query.set("q", "id:" + product.getId());
        QueryResponse response = solr.query(query);
        SolrDocumentList documentList = response.getResults();
        assertEquals(documentList.getNumFound(), 0);
    }
}
