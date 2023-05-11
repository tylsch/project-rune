package com.rune.harmonia.tests

import akka.cluster.MemberStatus
import akka.cluster.typed.Cluster
import com.rune.harmonia.Main
import org.scalatest.concurrent.PatienceConfiguration

import scala.concurrent.duration.DurationInt

class IntegrationSpec
  extends NodeFixtureSpec(Set(8101, 8102, 8103), Set(2551, 2552, 2553), Set(9101, 9102, 9103), "integration-test.conf") {

  "DockerComposeContainer" should {
    "retrieve non-0 port for any of services" in {
      withContainers { composedContainers =>
        assert(composedContainers.getServicePort("postgres-db", 5432) > 0)
      }
    }
  }

  "Harmonia Cart Service" should {
    "init and join cluster" in {
      withNodes { (nodeFixtures, systems3) =>
        nodeFixtures.foreach(node => Main.init(node.testKit.system))

        // let the nodes join and become Up
        eventually(PatienceConfiguration.Timeout(15.seconds)) {
          systems3.foreach { sys =>
            Cluster(sys).selfMember.status should ===(MemberStatus.Up)
            Cluster(sys).state.members.unsorted.map(_.status) should ===(Set(MemberStatus.Up))
          }
        }
      }
    }
  }

  "run integration test" should {
    "succeed without issue" in {
      println("Running tests in integration test scope")
      succeed
    }
  }
}