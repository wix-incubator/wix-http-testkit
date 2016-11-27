package com.wix.hoopoe.http.client.transformers

import akka.http.scaladsl.client.RequestBuilding.RequestTransformer
import akka.http.scaladsl.model.Uri.Query

trait HttpClientRequestTransformers {

  def withParams(params: (String, String)*): RequestTransformer = {

    r =>

      val u = r.uri
      u.withQuery(Query())

//      r.uri.copy(path = r.uri.path.cop )

      r.copy(uri = r.uri
                       .copy(path = r.uri.path /*query = Query(r.uri.query ++ params: _*)*/))
  }

}
