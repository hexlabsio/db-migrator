import io.hexlabs.kloudformation.module.serverless.Serverless
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.json
import io.kloudformation.resource.aws.ec2.securityGroup
import io.kloudformation.unaryPlus

class Stack : StackBuilder {

    override fun KloudFormation.create(args: List<String>) {
        val vpcId = +"vpc-35efcd53"
        val subnets = +listOf(
            +"subnet-a9d34ccf",
            +"subnet-cfc11895",
            +"subnet-c38de28b"
        )
        val securityGroup = securityGroup(+"Database Migrator SG") { vpcId(vpcId) }
        val codeLocation = args.first()
        serverless("database-migrator", "live", +"hexlabs-deployments") {
            serverlessFunction(
                    functionId = "database-migrator",
                    codeLocationKey = +codeLocation,
                    handler = +"io.klouds.migrator.RootHandler",
                    runtime = +"java8",
                    privateConfig = Serverless.PrivateConfig(+listOf(securityGroup.GroupId()), subnets)
            ) {
                lambdaFunction {
                    timeout(30)
                    memorySize(2048)
                    environment { variables(json(mapOf(
                        "HTTP4K_BOOTSTRAP_CLASS" to "io.klouds.migrator.RootApi"
                    ))) }
                }
            }
        }
    }
}